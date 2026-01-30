package com.endpoint.security.ingestion.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class NormalizedEventRecordSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void roundTrip_nonAlert() throws Exception {
        NormalizedEventRecord record = new NormalizedEventRecord(
            Instant.parse("2024-01-15T10:00:00Z"),
            "ep-123",
            "process_start",
            "user_1",
            "cmd.exe",
            false,
            null,
            null
        );
        String json = mapper.writeValueAsString(record);
        NormalizedEventRecord read = mapper.readValue(json, NormalizedEventRecord.class);
        assertThat(read.timestamp()).isEqualTo(record.timestamp());
        assertThat(read.endpointId()).isEqualTo("ep-123");
        assertThat(read.eventType()).isEqualTo("process_start");
        assertThat(read.isAlert()).isFalse();
    }

    @Test
    void roundTrip_alert() throws Exception {
        NormalizedEventRecord record = new NormalizedEventRecord(
            Instant.parse("2024-01-15T10:00:00Z"),
            "ep-456",
            "file_access",
            "user_2",
            "chrome.exe",
            true,
            85,
            "Suspicious"
        );
        String json = mapper.writeValueAsString(record);
        NormalizedEventRecord read = mapper.readValue(json, NormalizedEventRecord.class);
        assertThat(read.isAlert()).isTrue();
        assertThat(read.alertScore()).isEqualTo(85);
        assertThat(read.alertReason()).isEqualTo("Suspicious");
    }

    @Test
    void deserialize_fromNdjsonLine() throws Exception {
        String line = "{\"timestamp\":\"2024-01-15T10:00:00.000Z\",\"endpointId\":\"a\",\"eventType\":\"network_connection\",\"userId\":\"u\",\"processName\":\"p\",\"isAlert\":false}";
        NormalizedEventRecord r = mapper.readValue(line, NormalizedEventRecord.class);
        assertThat(r.endpointId()).isEqualTo("a");
        assertThat(r.eventType()).isEqualTo("network_connection");
    }
}
