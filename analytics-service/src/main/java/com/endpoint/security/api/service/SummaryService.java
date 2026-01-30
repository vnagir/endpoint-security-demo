package com.endpoint.security.api.service;

import com.endpoint.security.api.dto.SummaryResponse;
import com.endpoint.security.api.entity.SecurityEventEntity;
import com.endpoint.security.api.repository.SecurityEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SummaryService {

    private final SecurityEventRepository repository;

    public SummaryService(SecurityEventRepository repository) {
        this.repository = repository;
    }

    public SummaryResponse getSummary(String endpointId) {
        List<SecurityEventEntity> events = repository.findByEndpointIdOrderByTimestampDesc(endpointId);
        if (events.isEmpty()) {
            return null;
        }
        long total = events.size();
        String mostFrequent = events.stream()
            .collect(Collectors.groupingBy(SecurityEventEntity::getProcessName, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        Map<String, Long> breakdown = events.stream()
            .collect(Collectors.groupingBy(SecurityEventEntity::getEventType, Collectors.counting()));
        return new SummaryResponse(endpointId, total, mostFrequent, breakdown);
    }
}
