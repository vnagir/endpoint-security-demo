package com.endpoint.security.api.repository;

import com.endpoint.security.api.entity.SecurityEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SecurityEventRepository extends JpaRepository<SecurityEventEntity, Long> {

    List<SecurityEventEntity> findByEndpointIdOrderByTimestampDesc(String endpointId);

    @Query("SELECT DISTINCT e.endpointId FROM SecurityEventEntity e ORDER BY e.endpointId")
    List<String> findDistinctEndpointIds();
}
