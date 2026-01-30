package com.endpoint.security.collector;

import com.endpoint.security.collector.model.RawSecurityEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CollectorRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CollectorRunner.class);
    private final RawEventGenerator generator;
    private final RawEventWriter writer;
    private final long intervalMs;
    private volatile boolean running = true;
    private String endpointId;

    public CollectorRunner(RawEventGenerator generator,
                           RawEventWriter writer,
                           @org.springframework.beans.factory.annotation.Value("${collector.interval-ms:1000}") long intervalMs) {
        this.generator = generator;
        this.writer = writer;
        this.intervalMs = intervalMs;
        this.endpointId = generator.generateEndpointId();
    }

    @Override
    public void run(String... args) throws Exception {
        writer.start();
        log.info("Collector started, endpointId={}", endpointId);
        Thread t = new Thread(() -> {
            long eventCount = 0;
            while (running) {
                try {
                    RawSecurityEvent event = generator.generate(endpointId);
                    writer.write(event);
                    eventCount++;
                    if (eventCount % 30 == 0) {
                        log.info("Collector progress: {} events written for endpointId={}", eventCount, endpointId);
                    }
                    Thread.sleep(intervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.info("Collector loop interrupted after {} events", eventCount);
                    break;
                }
            }
        }, "CollectorLoop");
        t.setDaemon(false);
        t.start();
    }
}
