package com.endpoint.security.normalizer;

import com.endpoint.security.normalizer.model.SecurityEvent;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * Normalizes raw JSON event to SecurityEvent. Handles flexible input (timestamp, field names, casing).
 */
public class EventNormalizer {

    private static final Logger log = LoggerFactory.getLogger(EventNormalizer.class);
    private static final Set<String> VALID_EVENT_TYPES = Set.of("process_start", "file_access", "network_connection");
    private static final Instant MIN_VALID = Instant.parse("2000-01-01T00:00:00Z");

    private final Set<String> denylist;

    public EventNormalizer(Set<String> denylist) {
        this.denylist = denylist;
    }

    public SecurityEvent normalize(JsonNode node) {
        if (node == null || !node.isObject()) return null;

        String endpointId = text(node, "endpointId", "endpoint_id");
        if (endpointId == null || endpointId.isBlank()) return null;
        if (!isValidUuid(endpointId)) return null;

        String eventTypeRaw = text(node, "eventType", "event_type");
        if (eventTypeRaw == null || eventTypeRaw.isBlank()) return null;
        String eventType = eventTypeRaw.trim().toLowerCase();
        if (!VALID_EVENT_TYPES.contains(eventType)) return null;

        String userId = text(node, "userId", "user");
        if (userId == null || userId.isBlank()) return null;

        String processName = text(node, "processName", "process");
        if (processName == null || processName.isBlank()) return null;
        if (denylist.contains(processName.toLowerCase().trim())) return null;

        Instant ts = parseTimestamp(node.get("timestamp"));
        if (ts == null) ts = Instant.now();

        Integer alertScore = node.has("alertScore") && !node.get("alertScore").isNull()
            ? node.get("alertScore").asInt() : null;
        if (alertScore != null && (alertScore < 1 || alertScore > 100)) return null;

        String alertReason = node.has("alertReason") && !node.get("alertReason").isNull()
            ? node.get("alertReason").asText() : null;

        boolean isAlert = alertScore != null;

        if (isAlert) {
            return new SecurityEvent(ts, endpointId, eventType, userId, processName, alertScore, alertReason);
        } else {
            return new SecurityEvent(ts, endpointId, eventType, userId, processName);
        }
    }

    private static String text(JsonNode n, String... keys) {
        for (String k : keys) {
            if (n.has(k) && !n.get(k).isNull()) return n.get(k).asText();
        }
        return null;
    }

    private static boolean isValidUuid(String s) {
        try {
            UUID.fromString(s.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static Instant parseTimestamp(com.fasterxml.jackson.databind.JsonNode tsNode) {
        if (tsNode == null || tsNode.isNull()) return null;
        try {
            if (tsNode.isNumber()) {
                long ms = tsNode.asLong();
                if (ms < 1e12) ms *= 1000;
                return Instant.ofEpochMilli(ms);
            }
            String str = tsNode.asText();
            if (str == null || str.isBlank()) return null;
            try {
                return Instant.parse(str);
            } catch (Exception e) {
                return Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(str.replace(" ", "T")));
            }
        } catch (Exception e) {
            return null;
        }
    }
}
