package com.endpoint.security.api.service;

import com.endpoint.security.api.document.AlertDocument;
import com.endpoint.security.api.dto.PageResponse;
import com.endpoint.security.api.repository.AlertDocumentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertService {

    private static final int MAX_PAGE_SIZE = 100;

    private final AlertDocumentRepository repository;

    public AlertService(AlertDocumentRepository repository) {
        this.repository = repository;
    }

    public PageResponse<AlertDocument> getAlerts(String endpointId, Integer minScore, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(MAX_PAGE_SIZE, Math.max(1, size));
        Pageable pageable = PageRequest.of(safePage, safeSize);

        List<AlertDocument> content;
        long total;

        if (endpointId != null && !endpointId.isBlank() && minScore != null && minScore >= 1) {
            content = repository.findByEndpointIdAndAlertScoreGreaterThanEqualOrderByTimestampDesc(
                endpointId, minScore, pageable);
            total = repository.countByEndpointIdAndAlertScoreGreaterThanEqual(endpointId, minScore);
        } else if (endpointId != null && !endpointId.isBlank()) {
            content = repository.findByEndpointIdOrderByTimestampDesc(endpointId, pageable);
            total = repository.countByEndpointId(endpointId);
        } else if (minScore != null && minScore >= 1) {
            content = repository.findByAlertScoreGreaterThanEqualOrderByTimestampDesc(minScore, pageable);
            total = repository.countByAlertScoreGreaterThanEqual(minScore);
        } else {
            var p = repository.findAll(pageable);
            content = p.getContent();
            total = p.getTotalElements();
        }

        int totalPages = total == 0 ? 0 : (int) ((total + safeSize - 1) / safeSize);
        int currentPage1Based = total == 0 ? 0 : safePage + 1;
        return PageResponse.of(total, totalPages, currentPage1Based, safeSize, content);
    }
}
