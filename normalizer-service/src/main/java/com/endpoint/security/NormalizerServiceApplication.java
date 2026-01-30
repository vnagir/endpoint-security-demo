package com.endpoint.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NormalizerServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(NormalizerServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(NormalizerServiceApplication.class, args);
    }

    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void onReady() {
        log.info("Normalizer service ready; watching raw-events.ndjson -> normalized-events.ndjson");
    }
}
