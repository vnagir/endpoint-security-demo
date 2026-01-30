package com.endpoint.security.ingestion.repository;

import com.endpoint.security.ingestion.document.AlertDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AlertDocumentRepository extends MongoRepository<AlertDocument, String> {
}
