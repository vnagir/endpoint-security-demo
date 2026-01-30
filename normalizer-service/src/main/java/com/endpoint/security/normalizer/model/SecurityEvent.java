package com.endpoint.security.normalizer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SecurityEvent(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    Instant timestamp,
    String endpointId,
    String eventType,
    String userId,
    String processName,
    Boolean isAlert,
    Integer alertScore,
    String alertReason
) {
    public SecurityEvent(Instant timestamp, String endpointId, String eventType, String userId, String processName) {
        this(timestamp, endpointId, eventType, userId, processName, false, null, null);
    }

    public SecurityEvent(Instant timestamp, String endpointId, String eventType, String userId, String processName,
                         Integer alertScore, String alertReason) {
        this(timestamp, endpointId, eventType, userId, processName, true, alertScore, alertReason);
    }
}
