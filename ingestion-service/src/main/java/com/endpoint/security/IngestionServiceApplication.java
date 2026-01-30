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
@EnableJpaRepositories(basePackages = "com.endpoint.security.ingestion.repository")
@EnableMongoRepositories(basePackages = "com.endpoint.security.ingestion.repository")
public class IngestionServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(IngestionServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(IngestionServiceApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("Ingestion service ready; watching normalized-events.ndjson -> Postgres + MongoDB");
    }
}
