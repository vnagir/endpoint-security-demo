package com.endpoint.security.api.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Returns 401 Unauthorized with a JSON body when the request is unauthenticated
 * (missing or invalid API key / Bearer token).
 */
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(JsonAuthenticationEntryPoint.class);

    private static final String MESSAGE = "Missing or invalid API key. Provide X-API-Key header or Authorization: Bearer <token>.";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        log.warn("Unauthorized access to {} {}: {}", request.getMethod(), request.getRequestURI(), authException.getMessage());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        JsonErrorResponse.write(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", MESSAGE);
    }
}
