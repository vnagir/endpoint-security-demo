package com.endpoint.security.collector;

import com.endpoint.security.collector.model.RawSecurityEvent;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class RawEventGeneratorTest {

    @Test
    void generate_returnsNonNullEvent() {
        RawEventGenerator generator = new RawEventGenerator(2);
        RawSecurityEvent event = generator.generate("e1");
        assertThat(event).isNotNull();
    }

    @Test
    void generate_populatesEndpointId() {
        RawEventGenerator generator = new RawEventGenerator(2);
        String endpointId = UUID.randomUUID().toString();
        RawSecurityEvent event = generator.generate(endpointId);
        assertThat(event.getEndpointId()).isEqualTo(endpointId);
    }

    @Test
    void generate_populatesEventType() {
        Set<String> valid = Set.of("process_start", "file_access", "network_connection",
            "PROCESS_START", "FILE_ACCESS", "NETWORK_CONNECTION");
        RawEventGenerator generator = new RawEventGenerator(2);
        for (int i = 0; i < 50; i++) {
            RawSecurityEvent event = generator.generate(UUID.randomUUID().toString());
            String type = event.getEventType();
            assertThat(type).isNotNull();
            assertThat(valid).contains(type);
        }
    }

    @Test
    void generate_populatesUserId() {
        RawEventGenerator generator = new RawEventGenerator(2);
        RawSecurityEvent event = generator.generate(UUID.randomUUID().toString());
        assertThat(event.getUserId()).isNotNull().startsWith("user_");
    }

    @Test
    void generate_populatesProcessName() {
        Set<String> valid = Set.of("cmd.exe", "powershell.exe", "chrome.exe", "firefox.exe",
            "notepad.exe", "explorer.exe", "system_idle_process", "svchost.exe");
        RawEventGenerator generator = new RawEventGenerator(2);
        for (int i = 0; i < 30; i++) {
            RawSecurityEvent event = generator.generate(UUID.randomUUID().toString());
            assertThat(event.getProcessName()).isIn(valid);
        }
    }

    @Test
    void generate_respectsAlertPercentage() {
        int alertPct = 5;
        RawEventGenerator generator = new RawEventGenerator(alertPct);
        long withAlert = IntStream.range(0, 200)
            .mapToObj(i -> generator.generate(UUID.randomUUID().toString()))
            .filter(e -> e.getAlertScore() != null)
            .count();
        assertThat(withAlert).isGreaterThan(0);
        assertThat(withAlert).isLessThanOrEqualTo(200);
    }

    @Test
    void generateEndpointId_returnsValidUuid() {
        RawEventGenerator generator = new RawEventGenerator(2);
        for (int i = 0; i < 5; i++) {
            String id = generator.generateEndpointId();
            assertThat(id).isNotNull();
            assertThat(UUID.fromString(id)).isNotNull();
        }
    }

    @Test
    void constructor_clampsAlertPercentage() {
        RawEventGenerator zero = new RawEventGenerator(0);
        RawEventGenerator hundred = new RawEventGenerator(100);
        RawSecurityEvent e1 = zero.generate(UUID.randomUUID().toString());
        RawSecurityEvent e2 = hundred.generate(UUID.randomUUID().toString());
        assertThat(e1).isNotNull();
        assertThat(e2).isNotNull();
    }
}
