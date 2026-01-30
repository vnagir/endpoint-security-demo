package com.endpoint.security.normalizer;

import com.endpoint.security.normalizer.model.SecurityEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class NormalizerRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(NormalizerRunner.class);
    private final String rawPath;
    private final String normalizedPath;
    private final EventNormalizer normalizer;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public NormalizerRunner(
            @org.springframework.beans.factory.annotation.Value("${normalizer.input.path:./output/raw-events.ndjson}") String rawPath,
            @org.springframework.beans.factory.annotation.Value("${normalizer.output.path:./output/normalized-events.ndjson}") String normalizedPath,
            @org.springframework.beans.factory.annotation.Value("${normalizer.denylist:system_idle_process,svchost.exe}") String denylistStr) {
        this.rawPath = rawPath;
        this.normalizedPath = normalizedPath;
        Set<String> denylist = new HashSet<>(Arrays.asList(denylistStr.split(",")));
        this.normalizer = new EventNormalizer(denylist);
    }

    @Override
    public void run(String... args) throws Exception {
        Path raw = Paths.get(rawPath);
        Path out = Paths.get(normalizedPath);
        if (out.getParent() != null) Files.createDirectories(out.getParent());

        log.info("Normalizer started: {} -> {}", rawPath, normalizedPath);
        Thread t = new Thread(() -> {
            long lastSize = 0;
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    if (Files.exists(raw)) {
                        long len = Files.size(raw);
                        if (len > lastSize) {
                            try (BufferedReader reader = Files.newBufferedReader(raw);
                                 BufferedWriter writer = Files.newBufferedWriter(out,
                                         java.nio.file.StandardOpenOption.CREATE,
                                         java.nio.file.StandardOpenOption.APPEND)) {
                                long toSkip = lastSize;
                                while (toSkip > 0) {
                                    long s = reader.skip(toSkip);
                                    if (s <= 0) break;
                                    toSkip -= s;
                                }
                                String line;
                                int normalized = 0;
                                while ((line = reader.readLine()) != null) {
                                    if (line.isBlank()) continue;
                                    try {
                                        JsonNode node = objectMapper.readTree(line);
                                        SecurityEvent ev = normalizer.normalize(node);
                                        if (ev != null) {
                                            writer.write(objectMapper.writeValueAsString(ev));
                                            writer.newLine();
                                            normalized++;
                                        }
                                    } catch (Exception e) {
                                        log.trace("Skip line: {}", e.getMessage());
                                    }
                                }
                                writer.flush();
                                if (normalized > 0) {
                                    log.info("Normalizer processed {} new lines, total file size {} bytes", normalized, len);
                                }
                            }
                            lastSize = len;
                        }
                    }
                    Thread.sleep(2000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Normalizer error", e);
            }
        }, "NormalizerLoop");
        t.setDaemon(false);
        t.start();
    }
}
