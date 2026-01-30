CREATE TABLE IF NOT EXISTS security_events (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    endpoint_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    process_name VARCHAR(255) NOT NULL,
    is_alert BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_endpoint_timestamp ON security_events(endpoint_id, timestamp);
CREATE INDEX IF NOT EXISTS idx_timestamp ON security_events(timestamp);
