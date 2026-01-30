package com.endpoint.security.api.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * Returns 403 Forbidden with a JSON body when the user is authenticated
 * but not authorized for the resource (e.g. Analyst calling /alerts).
 */
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(JsonAccessDeniedHandler.class);

    private static final String MESSAGE = "Access denied. You do not have permission to access this resource.";

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.warn("Access denied for {} {}: {}", request.getMethod(), request.getRequestURI(), accessDeniedException.getMessage());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        JsonErrorResponse.write(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden", MESSAGE);
    }
}
