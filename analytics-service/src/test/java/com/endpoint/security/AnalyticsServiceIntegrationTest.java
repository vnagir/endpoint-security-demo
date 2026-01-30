package com.endpoint.security;

import com.endpoint.security.api.document.AlertDocument;
import com.endpoint.security.api.entity.SecurityEventEntity;
import com.endpoint.security.api.repository.AlertDocumentRepository;
import com.endpoint.security.api.repository.SecurityEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = AnalyticsServiceApplication.class)
@AutoConfigureMockMvc
@Testcontainers
class AnalyticsServiceIntegrationTest {

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
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    SecurityEventRepository eventRepository;

    @Autowired
    AlertDocumentRepository alertRepository;

    private static final String ENDPOINT_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String ANALYST_KEY = "analyst-token-67890";
    private static final String ADMIN_KEY = "admin-token-12345";

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        alertRepository.deleteAll();
    }

    @Test
    void getSummary_unauthorized_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/summary/" + ENDPOINT_ID))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getSummary_analystKey_returnsSummary() throws Exception {
        SecurityEventEntity e = new SecurityEventEntity();
        e.setTimestamp(Instant.now());
        e.setEndpointId(ENDPOINT_ID);
        e.setEventType("process_start");
        e.setUserId("u1");
        e.setProcessName("cmd.exe");
        e.setAlert(false);
        eventRepository.save(e);

        mockMvc.perform(get("/api/v1/summary/" + ENDPOINT_ID)
                .header("X-API-Key", ANALYST_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.endpointId").value(ENDPOINT_ID))
            .andExpect(jsonPath("$.totalEvents").value(1))
            .andExpect(jsonPath("$.mostFrequentProcess").value("cmd.exe"));
    }

    @Test
    void getSummary_missingEndpointId_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/summary")
                .header("X-API-Key", ANALYST_KEY))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value(containsString("Endpoint ID is required")));
    }

    @Test
    void getSummary_invalidUuid_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/summary/not-a-uuid")
                .header("X-API-Key", ANALYST_KEY))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value(containsString("Invalid endpoint ID")));
    }

    @Test
    void getSummary_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/summary/550e8400-e29b-41d4-a716-446655440099")
                .header("X-API-Key", ANALYST_KEY))
            .andExpect(status().isNotFound());
    }

    @Test
    void getAlerts_adminKey_returnsPaginatedAlerts() throws Exception {
        AlertDocument doc = new AlertDocument();
        doc.setTimestamp(Instant.now());
        doc.setEndpointId(ENDPOINT_ID);
        doc.setEventType("file_access");
        doc.setUserId("u2");
        doc.setProcessName("chrome.exe");
        doc.setAlertScore(80);
        doc.setAlertReason("Suspicious");
        alertRepository.save(doc);

        mockMvc.perform(get("/api/v1/alerts").param("page", "0").param("size", "10")
                .header("X-API-Key", ADMIN_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalResultCount").value(1))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.currentPage").value(1))
            .andExpect(jsonPath("$.countPerPage").value(10))
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].endpointId").value(ENDPOINT_ID))
            .andExpect(jsonPath("$.content[0].alertScore").value(80));
    }

    @Test
    void getAlerts_analystKey_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/alerts")
                .header("X-API-Key", ANALYST_KEY))
            .andExpect(status().isForbidden());
    }

    @Test
    void getEndpoints_analystKey_returnsPaginatedResponse() throws Exception {
        mockMvc.perform(get("/api/v1/endpoints").param("page", "0").param("size", "20")
                .header("X-API-Key", ANALYST_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalResultCount").exists())
            .andExpect(jsonPath("$.totalPages").exists())
            .andExpect(jsonPath("$.countPerPage").value(20))
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getEndpoints_withData_returnsEndpointIdsInContent() throws Exception {
        SecurityEventEntity e = new SecurityEventEntity();
        e.setTimestamp(Instant.now());
        e.setEndpointId(ENDPOINT_ID);
        e.setEventType("process_start");
        e.setUserId("u1");
        e.setProcessName("cmd.exe");
        e.setAlert(false);
        eventRepository.save(e);

        mockMvc.perform(get("/api/v1/endpoints").param("page", "0").param("size", "20")
                .header("X-API-Key", ANALYST_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalResultCount").value(1))
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0]").value(ENDPOINT_ID));
    }

    @Test
    void getEndpoints_unauthorized_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/endpoints"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void actuator_health_permittedWithoutAuth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
    }
}
