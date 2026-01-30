package com.endpoint.security.ingestion.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NormalizedEventRecord(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    Instant timestamp,
    String endpointId,
    String eventType,
    String userId,
    String processName,
    Boolean isAlert,
    Integer alertScore,
    String alertReason
) {}
