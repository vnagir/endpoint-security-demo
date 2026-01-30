package com.endpoint.security.collector.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Raw security event - may have inconsistent fields (null, different names/casing).
 * Serializes to JSON as-is for raw-events.ndjson.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class RawSecurityEvent {

    private Object timestamp;  // String or Long or null - intentionally flexible
    @JsonProperty("endpointId")
    private String endpointId;
    @JsonProperty("endpoint_id")
    private String endpoint_id;
    @JsonProperty("eventType")
    private String eventType;
    @JsonProperty("event_type")
    private String event_type;
    @JsonProperty("userId")
    private String userId;
    @JsonProperty("user")
    private String user;
    @JsonProperty("processName")
    private String processName;
    @JsonProperty("process")
    private String process;
    @JsonProperty("alertScore")
    private Integer alertScore;
    @JsonProperty("alertReason")
    private String alertReason;

    public Object getTimestamp() { return timestamp; }
    public void setTimestamp(Object timestamp) { this.timestamp = timestamp; }
    public String getEndpointId() { return endpointId != null ? endpointId : endpoint_id; }
    public void setEndpointId(String endpointId) { this.endpointId = endpointId; }
    public void setEndpoint_id(String endpoint_id) { this.endpoint_id = endpoint_id; }
    public String getEventType() { return eventType != null ? eventType : event_type; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setEvent_type(String event_type) { this.event_type = event_type; }
    public String getUserId() { return userId != null ? userId : user; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUser(String user) { this.user = user; }
    public String getProcessName() { return processName != null ? processName : process; }
    public void setProcessName(String processName) { this.processName = processName; }
    public void setProcess(String process) { this.process = process; }
    public Integer getAlertScore() { return alertScore; }
    public void setAlertScore(Integer alertScore) { this.alertScore = alertScore; }
    public String getAlertReason() { return alertReason; }
    public void setAlertReason(String alertReason) { this.alertReason = alertReason; }
}
