package com.endpoint.security.api.repository;

import com.endpoint.security.api.document.AlertDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AlertDocumentRepository extends MongoRepository<AlertDocument, String> {

    List<AlertDocument> findByEndpointIdOrderByTimestampDesc(String endpointId, Pageable pageable);

    List<AlertDocument> findByAlertScoreGreaterThanEqualOrderByTimestampDesc(int minScore, Pageable pageable);

    List<AlertDocument> findByEndpointIdAndAlertScoreGreaterThanEqualOrderByTimestampDesc(
            String endpointId, int minScore, Pageable pageable);

    long countByEndpointId(String endpointId);

    long countByAlertScoreGreaterThanEqual(int minScore);

    long countByEndpointIdAndAlertScoreGreaterThanEqual(String endpointId, int minScore);
}
