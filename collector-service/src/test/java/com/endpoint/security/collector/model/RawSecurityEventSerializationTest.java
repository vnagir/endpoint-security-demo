package com.endpoint.security.collector.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RawSecurityEventSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void roundTrip_camelCase() throws Exception {
        RawSecurityEvent event = new RawSecurityEvent();
        event.setTimestamp(Instant.now().toString());
        event.setEndpointId("ep-123");
        event.setEventType("process_start");
        event.setUserId("user_1");
        event.setProcessName("cmd.exe");

        String json = mapper.writeValueAsString(event);
        assertThat(json).contains("endpointId", "eventType", "userId", "processName");

        RawSecurityEvent read = mapper.readValue(json, RawSecurityEvent.class);
        assertThat(read.getEndpointId()).isEqualTo("ep-123");
        assertThat(read.getEventType()).isEqualTo("process_start");
        assertThat(read.getUserId()).isEqualTo("user_1");
        assertThat(read.getProcessName()).isEqualTo("cmd.exe");
    }

    @Test
    void roundTrip_snakeCaseAndAlertFields() throws Exception {
        RawSecurityEvent event = new RawSecurityEvent();
        event.setTimestamp(System.currentTimeMillis());
        event.setEndpoint_id("ep-456");
        event.setEvent_type("FILE_ACCESS");
        event.setUser("user_2");
        event.setProcess("chrome.exe");
        event.setAlertScore(75);
        event.setAlertReason("Suspicious pattern");

        String json = mapper.writeValueAsString(event);
        RawSecurityEvent read = mapper.readValue(json, RawSecurityEvent.class);
        assertThat(read.getEndpointId()).isEqualTo("ep-456");
        assertThat(read.getEventType()).isEqualTo("FILE_ACCESS");
        assertThat(read.getUserId()).isEqualTo("user_2");
        assertThat(read.getProcessName()).isEqualTo("chrome.exe");
        assertThat(read.getAlertScore()).isEqualTo(75);
        assertThat(read.getAlertReason()).isEqualTo("Suspicious pattern");
    }

    @Test
    void deserialize_fromMinimalJson() throws Exception {
        String json = "{\"timestamp\":\"2024-01-15T10:00:00Z\",\"endpointId\":\"a\",\"eventType\":\"network_connection\",\"userId\":\"u\",\"processName\":\"p\"}";
        RawSecurityEvent read = mapper.readValue(json, RawSecurityEvent.class);
        assertThat(read.getEndpointId()).isEqualTo("a");
        assertThat(read.getEventType()).isEqualTo("network_connection");
        assertThat(read.getUserId()).isEqualTo("u");
        assertThat(read.getProcessName()).isEqualTo("p");
    }
}
