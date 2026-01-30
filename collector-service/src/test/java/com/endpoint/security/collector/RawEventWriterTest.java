package com.endpoint.security.collector;

import com.endpoint.security.collector.model.RawSecurityEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RawEventWriterTest {

    @TempDir
    Path tempDir;

    private RawEventWriter writer;

    @AfterEach
    void tearDown() {
        if (writer != null) {
            writer.stop();
        }
    }

    @Test
    void write_appendsNdjsonLines() throws Exception {
        Path output = tempDir.resolve("raw-events.ndjson");
        writer = new RawEventWriter(output.toAbsolutePath().toString());
        writer.start();

        RawSecurityEvent e1 = new RawSecurityEvent();
        e1.setTimestamp(Instant.now().toString());
        e1.setEndpointId(UUID.randomUUID().toString());
        e1.setEventType("process_start");
        e1.setUserId("user_1");
        e1.setProcessName("cmd.exe");

        writer.write(e1);
        writer.write(e1);
        // Wait for background writer thread to drain queue and flush
        for (int i = 0; i < 50; i++) {
            if (Files.exists(output) && Files.readAllLines(output).size() >= 2) break;
            Thread.sleep(100);
        }
        writer.stop();
        Thread.sleep(200);

        List<String> lines = Files.readAllLines(output);
        assertThat(lines).hasSize(2);
        ObjectMapper mapper = new ObjectMapper();
        for (String line : lines) {
            assertThat(line).isNotBlank();
            RawSecurityEvent read = mapper.readValue(line, RawSecurityEvent.class);
            assertThat(read.getEndpointId()).isEqualTo(e1.getEndpointId());
            assertThat(read.getEventType()).isEqualTo("process_start");
        }
    }

    @Test
    void write_ignoresNull() throws Exception {
        Path output = tempDir.resolve("out.ndjson");
        writer = new RawEventWriter(output.toAbsolutePath().toString());
        writer.start();
        writer.write(null);
        writer.stop();
        Thread.sleep(200);
        assertThat(Files.readAllLines(output)).isEmpty();
    }
}
