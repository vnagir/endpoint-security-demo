package com.endpoint.security.api.service;

import com.endpoint.security.api.document.AlertDocument;
import com.endpoint.security.api.repository.AlertDocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    AlertDocumentRepository repository;

    @InjectMocks
    AlertService alertService;

    @Test
    void getAlerts_endpointAndMinScore_callsCorrectMethod() {
        List<AlertDocument> alerts = List.of(alertDoc("ep-1", 80));
        when(repository.findByEndpointIdAndAlertScoreGreaterThanEqualOrderByTimestampDesc(
            eq("ep-1"), eq(50), any(PageRequest.class))).thenReturn(alerts);
        when(repository.countByEndpointIdAndAlertScoreGreaterThanEqual("ep-1", 50)).thenReturn(1L);

        var result = alertService.getAlerts("ep-1", 50, 0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).getEndpointId()).isEqualTo("ep-1");
        assertThat(result.totalResultCount()).isEqualTo(1);
        assertThat(result.currentPage()).isEqualTo(1);
    }

    @Test
    void getAlerts_endpointOnly_callsFindByEndpoint() {
        List<AlertDocument> alerts = List.of(alertDoc("ep-1", 70));
        when(repository.findByEndpointIdOrderByTimestampDesc(eq("ep-1"), any(PageRequest.class))).thenReturn(alerts);
        when(repository.countByEndpointId("ep-1")).thenReturn(1L);

        var result = alertService.getAlerts("ep-1", null, 0, 10);

        assertThat(result.content()).hasSize(1);
        verify(repository).findByEndpointIdOrderByTimestampDesc(eq("ep-1"), any(PageRequest.class));
    }

    @Test
    void getAlerts_minScoreOnly_callsFindByScore() {
        List<AlertDocument> alerts = List.of(alertDoc("ep-2", 90));
        when(repository.findByAlertScoreGreaterThanEqualOrderByTimestampDesc(eq(60), any(PageRequest.class))).thenReturn(alerts);
        when(repository.countByAlertScoreGreaterThanEqual(60)).thenReturn(1L);

        var result = alertService.getAlerts(null, 60, 0, 10);

        assertThat(result.content()).hasSize(1);
        verify(repository).findByAlertScoreGreaterThanEqualOrderByTimestampDesc(eq(60), any(PageRequest.class));
    }

    @Test
    void getAlerts_noParams_returnsFindAllPage() {
        List<AlertDocument> alerts = List.of(alertDoc("ep-1", 70));
        when(repository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(alerts, PageRequest.of(0, 10), 1));

        var result = alertService.getAlerts(null, null, 0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalResultCount()).isEqualTo(1);
    }

    private static AlertDocument alertDoc(String endpointId, int score) {
        AlertDocument d = new AlertDocument();
        d.setTimestamp(Instant.now());
        d.setEndpointId(endpointId);
        d.setEventType("process_start");
        d.setUserId("u");
        d.setProcessName("cmd.exe");
        d.setAlertScore(score);
        d.setAlertReason("Suspicious");
        return d;
    }
}
