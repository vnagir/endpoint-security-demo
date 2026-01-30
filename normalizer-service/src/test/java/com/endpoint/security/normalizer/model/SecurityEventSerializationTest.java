package com.endpoint.security.normalizer.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityEventSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void roundTrip_nonAlert() throws Exception {
        SecurityEvent event = new SecurityEvent(
            Instant.parse("2024-01-15T10:00:00Z"),
            "550e8400-e29b-41d4-a716-446655440000",
            "process_start",
            "user_1",
            "cmd.exe"
        );
        String json = mapper.writeValueAsString(event);
        assertThat(json).contains("timestamp", "endpointId", "eventType", "processName");
        assertThat(json).doesNotContain("alertScore");

        SecurityEvent read = mapper.readValue(json, SecurityEvent.class);
        assertThat(read.timestamp()).isEqualTo(event.timestamp());
        assertThat(read.endpointId()).isEqualTo(event.endpointId());
        assertThat(read.eventType()).isEqualTo(event.eventType());
        assertThat(read.userId()).isEqualTo(event.userId());
        assertThat(read.processName()).isEqualTo(event.processName());
        assertThat(read.isAlert()).isFalse();
    }

    @Test
    void roundTrip_alert() throws Exception {
        SecurityEvent event = new SecurityEvent(
            Instant.parse("2024-01-15T10:00:00Z"),
            "550e8400-e29b-41d4-a716-446655440000",
            "file_access",
            "user_2",
            "chrome.exe",
            80,
            "Suspicious file access"
        );
        String json = mapper.writeValueAsString(event);
        SecurityEvent read = mapper.readValue(json, SecurityEvent.class);
        assertThat(read.isAlert()).isTrue();
        assertThat(read.alertScore()).isEqualTo(80);
        assertThat(read.alertReason()).isEqualTo("Suspicious file access");
    }

    @Test
    void deserialize_fromNdjsonLine() throws Exception {
        String line = "{\"timestamp\":\"2024-01-15T10:00:00.000Z\",\"endpointId\":\"a\",\"eventType\":\"network_connection\",\"userId\":\"u\",\"processName\":\"p\",\"isAlert\":false}";
        SecurityEvent ev = mapper.readValue(line, SecurityEvent.class);
        assertThat(ev.endpointId()).isEqualTo("a");
        assertThat(ev.eventType()).isEqualTo("network_connection");
        assertThat(ev.isAlert()).isFalse();
    }
}
