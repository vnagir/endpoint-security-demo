package com.endpoint.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.endpoint.security.api.repository")
@EnableMongoRepositories(basePackages = "com.endpoint.security.api.repository")
public class AnalyticsServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("Analytics service ready; API: GET /api/v1/endpoints, GET /api/v1/summary/{{endpointId}}, GET /api/v1/alerts");
    }
}
