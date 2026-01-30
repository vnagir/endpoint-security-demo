package com.endpoint.security.collector;

import com.endpoint.security.collector.model.RawSecurityEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class RawEventWriter {

    private static final Logger log = LoggerFactory.getLogger(RawEventWriter.class);
    private final ObjectMapper objectMapper = new ObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final String outputPath;
    private final BlockingQueue<RawSecurityEvent> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private BufferedWriter writer;
    private Thread writerThread;

    public RawEventWriter(@Value("${collector.output.path:./output/raw-events.ndjson}") String outputPath) {
        this.outputPath = outputPath;
    }

    public void start() throws IOException {
        Path path = Paths.get(outputPath);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        writer = new BufferedWriter(new java.io.FileWriter(outputPath, true));
        running.set(true);
        writerThread = new Thread(this::drainQueue, "RawEventWriter");
        writerThread.setDaemon(true);
        writerThread.start();
        log.info("RawEventWriter started, output: {}", outputPath);
    }

    public void write(RawSecurityEvent event) {
        if (event != null) {
            queue.offer(event);
        }
    }

    private void drainQueue() {
        while (running.get() || !queue.isEmpty()) {
            try {
                RawSecurityEvent event = queue.poll();
                if (event != null) {
                    String line = objectMapper.writeValueAsString(event);
                    writer.write(line);
                    writer.newLine();
                } else {
                    writer.flush();
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Write error", e);
            }
        }
        try {
            if (writer != null) writer.flush();
        } catch (IOException ignored) {}
    }

    @PreDestroy
    public void stop() {
        if (!stopped.compareAndSet(false, true)) {
            return; // already stopped (idempotent)
        }
        running.set(false);
        if (writerThread != null) {
            writerThread.interrupt();
        }
        try {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            log.warn("Error closing writer", e);
        }
    }
}
