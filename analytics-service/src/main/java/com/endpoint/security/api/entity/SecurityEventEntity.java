package com.endpoint.security.api.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "security_events", indexes = {
    @Index(name = "idx_endpoint_timestamp", columnList = "endpoint_id, timestamp"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
public class SecurityEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(name = "endpoint_id", nullable = false, length = 36)
    private String endpointId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "process_name", nullable = false, length = 255)
    private String processName;

    @Column(name = "is_alert", nullable = false)
    private boolean alert;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getEndpointId() { return endpointId; }
    public void setEndpointId(String endpointId) { this.endpointId = endpointId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    public boolean isAlert() { return alert; }
    public void setAlert(boolean alert) { this.alert = alert; }
}
