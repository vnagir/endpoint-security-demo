package com.endpoint.security.api.controller;

import com.endpoint.security.api.dto.SummaryResponse;
import com.endpoint.security.api.service.SummaryService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class SummaryController {

    private final SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Hidden
    public ResponseEntity<?> getSummaryMissingId() {
        throw new IllegalArgumentException("Endpoint ID is required. Please provide a valid UUID in the path, e.g. /api/v1/summary/{endpointId}");
    }

    @GetMapping("/summary/{endpointId}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<?> getSummary(@PathVariable String endpointId) {
        if (endpointId == null || endpointId.isBlank()) {
            throw new IllegalArgumentException("Endpoint ID is required. Please provide a valid UUID in the path, e.g. /api/v1/summary/{endpointId}");
        }
        try {
            UUID.fromString(endpointId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid endpoint ID; please provide a valid UUID");
        }
        SummaryResponse summary = summaryService.getSummary(endpointId);
        if (summary == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(summary);
    }
}
