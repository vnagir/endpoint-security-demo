package com.endpoint.security.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * Writes a consistent JSON error body for 401/403 responses.
 */
final class JsonErrorResponse {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonErrorResponse() {}

    static void write(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> body = Map.of(
            "error", error,
            "message", message != null ? message : "",
            "timestamp", Instant.now().toString()
        );
        MAPPER.writeValue(response.getOutputStream(), body);
    }
}
