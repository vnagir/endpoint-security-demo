package com.endpoint.security.api.controller;

import com.endpoint.security.api.document.AlertDocument;
import com.endpoint.security.api.dto.PageResponse;
import com.endpoint.security.api.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AlertDocument>> getAlerts(
            @RequestParam(required = false) String endpoint_id,
            @RequestParam(required = false) Integer min_score,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(alertService.getAlerts(endpoint_id, min_score, page, size));
    }
}
