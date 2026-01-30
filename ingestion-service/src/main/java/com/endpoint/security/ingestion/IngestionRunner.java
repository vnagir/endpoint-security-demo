package com.endpoint.security.ingestion;

import com.endpoint.security.ingestion.document.AlertDocument;
import com.endpoint.security.ingestion.entity.SecurityEventEntity;
import com.endpoint.security.ingestion.model.NormalizedEventRecord;
import com.endpoint.security.ingestion.repository.AlertDocumentRepository;
import com.endpoint.security.ingestion.repository.SecurityEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class IngestionRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(IngestionRunner.class);
    private static final int BATCH_SIZE = 100;

    private final String inputPath;
    private final int maxRecordsPerPoll;
    private final long pollIntervalMs;
    private final SecurityEventRepository eventRepo;
    private final AlertDocumentRepository alertRepo;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public IngestionRunner(
            @org.springframework.beans.factory.annotation.Value("${ingestion.input.path:./output/normalized-events.ndjson}") String inputPath,
            @org.springframework.beans.factory.annotation.Value("${ingestion.max-records-per-poll:0}") int maxRecordsPerPoll,
            @org.springframework.beans.factory.annotation.Value("${ingestion.poll-interval-ms:3000}") long pollIntervalMs,
            SecurityEventRepository eventRepo,
            AlertDocumentRepository alertRepo) {
        this.inputPath = inputPath;
        this.maxRecordsPerPoll = maxRecordsPerPoll;
        this.pollIntervalMs = pollIntervalMs;
        this.eventRepo = eventRepo;
        this.alertRepo = alertRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        Path path = Paths.get(inputPath);
        log.info("Ingestion started, input: {}, maxRecordsPerPoll: {} (0=no limit), pollIntervalMs: {}", inputPath, maxRecordsPerPoll, pollIntervalMs);
        Thread t = new Thread(() -> {
            long lastSize = 0;
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    if (Files.exists(path)) {
                        long len = Files.size(path);
                        if (len > lastSize) {
                            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                                long toSkip = lastSize;
                                while (toSkip > 0) {
                                    long s = reader.skip(toSkip);
                                    if (s <= 0) break;
                                    toSkip -= s;
                                }
                                List<SecurityEventEntity> eventsBatch = new ArrayList<>();
                                List<AlertDocument> alertsBatch = new ArrayList<>();
                                String line;
                                int processedThisPoll = 0;
                                long bytesReadThisPoll = 0;
                                while ((line = reader.readLine()) != null) {
                                    if (line.isBlank()) continue;
                                    try {
                                        NormalizedEventRecord r = objectMapper.readValue(line, NormalizedEventRecord.class);
                                        Boolean isAlert = r.isAlert() != null && r.isAlert();
                                        if (isAlert && r.alertScore() != null) {
                                            AlertDocument doc = new AlertDocument();
                                            doc.setTimestamp(r.timestamp());
                                            doc.setEndpointId(r.endpointId());
                                            doc.setEventType(r.eventType());
                                            doc.setUserId(r.userId());
                                            doc.setProcessName(r.processName());
                                            doc.setAlertScore(r.alertScore());
                                            doc.setAlertReason(r.alertReason());
                                            alertsBatch.add(doc);
                                        } else {
                                            SecurityEventEntity e = new SecurityEventEntity();
                                            e.setTimestamp(r.timestamp());
                                            e.setEndpointId(r.endpointId());
                                            e.setEventType(r.eventType());
                                            e.setUserId(r.userId());
                                            e.setProcessName(r.processName());
                                            e.setAlert(isAlert);
                                            eventsBatch.add(e);
                                        }
                                        processedThisPoll++;
                                        bytesReadThisPoll += line.getBytes(StandardCharsets.UTF_8).length + 1;
                                        if (maxRecordsPerPoll > 0 && processedThisPoll >= maxRecordsPerPoll) {
                                            log.info("Ingestion reached max records per poll ({}), stopping this cycle", maxRecordsPerPoll);
                                            break;
                                        }
                                        if (eventsBatch.size() + alertsBatch.size() >= BATCH_SIZE) {
                                            flush(eventsBatch, alertsBatch);
                                        }
                                    } catch (Exception e) {
                                        log.trace("Skip line: {}", e.getMessage());
                                    }
                                }
                                flush(eventsBatch, alertsBatch);
                                lastSize = (maxRecordsPerPoll > 0 && processedThisPoll >= maxRecordsPerPoll)
                                    ? lastSize + bytesReadThisPoll
                                    : len;
                            }
                        }
                    }
                    Thread.sleep(pollIntervalMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Ingestion error", e);
            }
        }, "IngestionLoop");
        t.setDaemon(false);
        t.start();
    }

    private void flush(List<SecurityEventEntity> eventsBatch, List<AlertDocument> alertsBatch) {
        if (!eventsBatch.isEmpty()) {
            eventRepo.saveAll(eventsBatch);
            log.info("Ingestion saved {} events to Postgres", eventsBatch.size());
            eventsBatch.clear();
        }
        if (!alertsBatch.isEmpty()) {
            alertRepo.saveAll(alertsBatch);
            log.info("Ingestion saved {} alerts to MongoDB", alertsBatch.size());
            alertsBatch.clear();
        }
    }
}
