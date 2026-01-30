package com.endpoint.security.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Logs each API request with method, path, status, and duration for traceability.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter implements Ordered {

    private static final int ORDER = -200;

    @Override
    public int getOrder() {
        return ORDER;
    }

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final AtomicLong REQUEST_ID = new AtomicLong(0);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!log.isInfoEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        long id = REQUEST_ID.incrementAndGet();
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("requestId={} method={} path={} status={} durationMs={}",
                id, request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path != null && path.startsWith("/actuator");
    }
}
