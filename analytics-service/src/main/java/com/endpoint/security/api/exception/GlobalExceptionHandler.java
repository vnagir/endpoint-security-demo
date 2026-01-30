package com.endpoint.security.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<Map<String, Object>> missingPathVariable(MissingPathVariableException e) {
        log.warn("Missing path variable: {}", e.getVariableName());
        String message = "endpointId".equals(e.getVariableName())
            ? "Endpoint ID is required. Please provide a valid UUID in the path, e.g. /api/v1/summary/{endpointId}"
            : "Required path variable '" + e.getVariableName() + "' is missing";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "error", "Bad Request",
            "message", message,
            "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> accessDenied(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "error", "Forbidden",
            "message", e.getMessage() != null ? e.getMessage() : "Access denied",
            "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException e) {
        log.warn("Bad request: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "error", "Bad Request",
            "message", e.getMessage(),
            "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> serverError(Exception e) {
        log.error("Unhandled error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "error", "Internal Server Error",
            "message", e.getMessage() != null ? e.getMessage() : "Unknown error",
            "timestamp", Instant.now().toString()
        ));
    }
}
