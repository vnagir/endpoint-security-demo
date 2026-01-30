package com.endpoint.security.ingestion.repository;

import com.endpoint.security.ingestion.entity.SecurityEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityEventRepository extends JpaRepository<SecurityEventEntity, Long> {
}
