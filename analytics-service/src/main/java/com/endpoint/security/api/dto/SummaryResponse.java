package com.endpoint.security.api.dto;

import java.util.Map;

public record SummaryResponse(
    String endpointId,
    long totalEvents,
    String mostFrequentProcess,
    Map<String, Long> eventTypeBreakdown
) {}
