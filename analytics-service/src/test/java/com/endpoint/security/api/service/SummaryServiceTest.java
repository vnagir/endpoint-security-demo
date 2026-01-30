package com.endpoint.security.api.service;

import com.endpoint.security.api.dto.SummaryResponse;
import com.endpoint.security.api.entity.SecurityEventEntity;
import com.endpoint.security.api.repository.SecurityEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SummaryServiceTest {

    @Mock
    SecurityEventRepository repository;

    @InjectMocks
    SummaryService summaryService;

    @Test
    void getSummary_empty_returnsNull() {
        when(repository.findByEndpointIdOrderByTimestampDesc("ep-123")).thenReturn(List.of());
        SummaryResponse result = summaryService.getSummary("ep-123");
        assertThat(result).isNull();
    }

    @Test
    void getSummary_events_returnsSummary() {
        String endpointId = "550e8400-e29b-41d4-a716-446655440000";
        SecurityEventEntity e1 = entity(endpointId, "process_start", "cmd.exe");
        SecurityEventEntity e2 = entity(endpointId, "process_start", "cmd.exe");
        SecurityEventEntity e3 = entity(endpointId, "file_access", "chrome.exe");
        when(repository.findByEndpointIdOrderByTimestampDesc(endpointId))
            .thenReturn(List.of(e1, e2, e3));

        SummaryResponse result = summaryService.getSummary(endpointId);

        assertThat(result).isNotNull();
        assertThat(result.endpointId()).isEqualTo(endpointId);
        assertThat(result.totalEvents()).isEqualTo(3);
        assertThat(result.mostFrequentProcess()).isEqualTo("cmd.exe");
        assertThat(result.eventTypeBreakdown()).containsEntry("process_start", 2L).containsEntry("file_access", 1L);
    }

    private static SecurityEventEntity entity(String endpointId, String eventType, String processName) {
        SecurityEventEntity e = new SecurityEventEntity();
        e.setId(1L);
        e.setTimestamp(Instant.now());
        e.setEndpointId(endpointId);
        e.setEventType(eventType);
        e.setUserId("u");
        e.setProcessName(processName);
        e.setAlert(false);
        return e;
    }
}
