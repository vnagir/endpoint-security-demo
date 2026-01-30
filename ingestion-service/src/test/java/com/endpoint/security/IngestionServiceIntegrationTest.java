package com.endpoint.security;

import com.endpoint.security.ingestion.document.AlertDocument;
import com.endpoint.security.ingestion.entity.SecurityEventEntity;
import com.endpoint.security.ingestion.repository.AlertDocumentRepository;
import com.endpoint.security.ingestion.repository.SecurityEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class IngestionServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    SecurityEventRepository eventRepository;

    @Autowired
    AlertDocumentRepository alertRepository;

    @Test
    void postgres_saveAndFindEvents() {
        SecurityEventEntity e = new SecurityEventEntity();
        e.setTimestamp(Instant.now());
        e.setEndpointId("550e8400-e29b-41d4-a716-446655440000");
        e.setEventType("process_start");
        e.setUserId("user_1");
        e.setProcessName("cmd.exe");
        e.setAlert(false);

        eventRepository.save(e);
        List<SecurityEventEntity> all = eventRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getEndpointId()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(all.get(0).getEventType()).isEqualTo("process_start");
    }

    @Test
    void mongodb_saveAndFindAlerts() {
        AlertDocument doc = new AlertDocument();
        doc.setTimestamp(Instant.now());
        doc.setEndpointId("550e8400-e29b-41d4-a716-446655440001");
        doc.setEventType("file_access");
        doc.setUserId("user_2");
        doc.setProcessName("chrome.exe");
        doc.setAlertScore(75);
        doc.setAlertReason("Suspicious");

        alertRepository.save(doc);
        List<AlertDocument> all = alertRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getEndpointId()).isEqualTo("550e8400-e29b-41d4-a716-446655440001");
        assertThat(all.get(0).getAlertScore()).isEqualTo(75);
    }
}
