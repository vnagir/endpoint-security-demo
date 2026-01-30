package com.endpoint.security.collector;

import com.endpoint.security.collector.model.RawSecurityEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

/**
 * Generates raw (intentionally messy) security events for simulation.
 * Output varies: timestamp format, field names (snake_case vs camelCase), casing, nulls.
 */
public class RawEventGenerator {

    private static final Logger log = LoggerFactory.getLogger(RawEventGenerator.class);
    private final Random random = new Random();
    private final int alertPercentage;

    private static final String[] EVENT_TYPES = { "process_start", "file_access", "network_connection" };
    private static final String[] EVENT_TYPES_RAW = { "PROCESS_START", "FILE_ACCESS", "NETWORK_CONNECTION" };
    private static final String[] PROCESS_NAMES = {
        "cmd.exe", "powershell.exe", "chrome.exe", "firefox.exe", "notepad.exe", "explorer.exe",
        "system_idle_process", "svchost.exe"
    };
    private static final String[] ALERT_REASONS = {
        "Suspicious process execution pattern detected",
        "Unauthorized file access attempt",
        "Unusual network connection to external IP"
    };

    public RawEventGenerator(int alertPercentage) {
        this.alertPercentage = Math.max(1, Math.min(5, alertPercentage));
    }

    public RawSecurityEvent generate(String endpointId) {
        RawSecurityEvent e = new RawSecurityEvent();
        int variant = random.nextInt(4);

        if (variant == 0) {
            e.setTimestamp(Instant.now().toString());
        } else if (variant == 1) {
            e.setTimestamp(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(Instant.now().atZone(java.time.ZoneId.of("UTC"))));
        } else if (variant == 2 && random.nextBoolean()) {
            e.setTimestamp(null);
        } else {
            e.setTimestamp(System.currentTimeMillis());
        }

        if (random.nextBoolean()) {
            e.setEndpointId(endpointId);
        } else {
            e.setEndpoint_id(endpointId);
        }

        String eventType = EVENT_TYPES[random.nextInt(EVENT_TYPES.length)];
        if (random.nextBoolean()) {
            e.setEventType(eventType);
        } else {
            e.setEvent_type(EVENT_TYPES_RAW[random.nextInt(EVENT_TYPES_RAW.length)]);
        }

        String userId = "user_" + random.nextInt(1000);
        if (random.nextBoolean()) {
            e.setUserId(userId);
        } else {
            e.setUser(userId);
        }

        String processName = PROCESS_NAMES[random.nextInt(PROCESS_NAMES.length)];
        if (random.nextBoolean()) {
            e.setProcessName(processName);
        } else {
            e.setProcess(processName);
        }

        boolean isAlert = random.nextInt(100) < alertPercentage;
        if (isAlert) {
            e.setAlertScore(random.nextInt(100) + 1);
            e.setAlertReason(ALERT_REASONS[random.nextInt(ALERT_REASONS.length)]);
        }

        return e;
    }

    public String generateEndpointId() {
        return UUID.randomUUID().toString();
    }
}
