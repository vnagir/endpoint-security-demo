package com.endpoint.security;

import com.endpoint.security.collector.RawEventGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class CollectorServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(CollectorServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CollectorServiceApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("Collector service ready; writing raw events to output");
    }

    @Bean
    public RawEventGenerator rawEventGenerator(
            @org.springframework.beans.factory.annotation.Value("${collector.alert-percentage:3}") int alertPercentage) {
        return new RawEventGenerator(alertPercentage);
    }
}
