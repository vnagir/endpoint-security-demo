package com.endpoint.security.normalizer;

import com.endpoint.security.normalizer.model.SecurityEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EventNormalizerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final EventNormalizer normalizer = new EventNormalizer(Set.of("system_idle_process", "svchost.exe"));

    @Test
    void normalize_validCamelCase_returnsEvent() throws Exception {
        String json = "{\"timestamp\":\"2024-01-15T10:00:00Z\",\"endpointId\":\"550e8400-e29b-41d4-a716-446655440000\",\"eventType\":\"process_start\",\"userId\":\"u1\",\"processName\":\"cmd.exe\"}";
        JsonNode node = mapper.readTree(json);
        SecurityEvent ev = normalizer.normalize(node);
        assertThat(ev).isNotNull();
        assertThat(ev.endpointId()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(ev.eventType()).isEqualTo("process_start");
        assertThat(ev.userId()).isEqualTo("u1");
        assertThat(ev.processName()).isEqualTo("cmd.exe");
        assertThat(ev.isAlert()).isFalse();
    }

    @Test
    void normalize_validSnakeCase_returnsEvent() throws Exception {
        String json = "{\"timestamp\":\"2024-01-15T10:00:00Z\",\"endpoint_id\":\"550e8400-e29b-41d4-a716-446655440000\",\"event_type\":\"FILE_ACCESS\",\"user\":\"u2\",\"process\":\"chrome.exe\"}";
        JsonNode node = mapper.readTree(json);
        SecurityEvent ev = normalizer.normalize(node);
        assertThat(ev).isNotNull();
        assertThat(ev.eventType()).isEqualTo("file_access");
        assertThat(ev.userId()).isEqualTo("u2");
        assertThat(ev.processName()).isEqualTo("chrome.exe");
    }

    @Test
    void normalize_invalidUuid_returnsNull() throws Exception {
        String json = "{\"timestamp\":\"2024-01-15T10:00:00Z\",\"endpointId\":\"not-a-uuid\",\"eventType\":\"process_start\",\"userId\":\"u\",\"processName\":\"cmd.exe\"}";
        JsonNode node = mapper.readTree(json);
        assertThat(normalizer.normalize(node)).isNull();
    }

    @Test
    void normalize_denylistProcess_returnsNull() throws Exception {
        String json = "{\"timestamp\":\"2024-01-15T10:00:00Z\",\"endpointId\":\"550e8400-e29b-41d4-a716-446655440000\",\"eventType\":\"process_start\",\"userId\":\"u\",\"processName\":\"system_idle_process\"}";
        JsonNode node = mapper.readTree(json);
        assertThat(normalizer.normalize(node)).isNull();
    }

    @Test
    void normalize_denylistSvchost_returnsNull() throws Exception {
        String json = "{\"timestamp\":\"2024-01-15T10:00:00Z\",\"endpointId\":\"550e8400-e29b-41d4-a716-446655440000\",\"eventType\":\"process_start\",\"userId\":\"u\",\"processName\":\"svchost.exe\"}";
        JsonNode node = mapper.readTree(json);
        assertThat(normalizer.normalize(node)).isNull();
    }

    @Test
    void normalize_invalidEventType_returnsNull() throws Exception {
        String json = "{\"timestamp\":\"2024-01-15T10:00:00Z\",\"endpointId\":\"550e8400-e29b-41d4-a716-446655440000\",\"eventType\":\"invalid_type\",\"userId\":\"u\",\"processName\":\"cmd.exe\"}";
        JsonNode node = mapper.readTree(json);
        assertThat(normalizer.normalize(node)).isNull();
    }

    @Test
    void normalize_alertScoreOutOfRange_returnsNull() throws Exception {
        String json = "{\"timestamp\":\"2024-01-15T10:00:00Z\",\"endpointId\":\"550e8400-e29b-41d4-a716-446655440000\",\"eventType\":\"process_start\",\"userId\":\"u\",\"processName\":\"cmd.exe\",\"alertScore\":150}";
        JsonNode node = mapper.readTree(json);
        assertThat(normalizer.normalize(node)).isNull();
    }

    @Test
    void normalize_validAlert_returnsEventWithAlert() throws Exception {
        String json = "{\"timestamp\":\"2024-01-15T10:00:00Z\",\"endpointId\":\"550e8400-e29b-41d4-a716-446655440000\",\"eventType\":\"process_start\",\"userId\":\"u\",\"processName\":\"cmd.exe\",\"alertScore\":75,\"alertReason\":\"Suspicious\"}";
        JsonNode node = mapper.readTree(json);
        SecurityEvent ev = normalizer.normalize(node);
        assertThat(ev).isNotNull();
        assertThat(ev.isAlert()).isTrue();
        assertThat(ev.alertScore()).isEqualTo(75);
        assertThat(ev.alertReason()).isEqualTo("Suspicious");
    }

    @Test
    void normalize_nullOrNonObject_returnsNull() {
        assertThat(normalizer.normalize(null)).isNull();
    }

    @Test
    void normalize_timestampAsEpochMs_parses() throws Exception {
        long ms = Instant.parse("2024-01-15T10:00:00Z").toEpochMilli();
        String json = "{\"timestamp\":" + ms + ",\"endpointId\":\"550e8400-e29b-41d4-a716-446655440000\",\"eventType\":\"process_start\",\"userId\":\"u\",\"processName\":\"cmd.exe\"}";
        JsonNode node = mapper.readTree(json);
        SecurityEvent ev = normalizer.normalize(node);
        assertThat(ev).isNotNull();
        assertThat(ev.timestamp()).isNotNull();
    }

    @Test
    void normalize_emptyDenylist_allowsAllProcesses() throws Exception {
        EventNormalizer noDeny = new EventNormalizer(Set.of());
        String json = "{\"timestamp\":\"2024-01-15T10:00:00Z\",\"endpointId\":\"550e8400-e29b-41d4-a716-446655440000\",\"eventType\":\"process_start\",\"userId\":\"u\",\"processName\":\"system_idle_process\"}";
        JsonNode node = mapper.readTree(json);
        SecurityEvent ev = noDeny.normalize(node);
        assertThat(ev).isNotNull();
        assertThat(ev.processName()).isEqualTo("system_idle_process");
    }
}
