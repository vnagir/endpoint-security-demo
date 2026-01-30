package com.endpoint.security.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.core.Ordered;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates via Bearer token or X-API-Key. Hardcoded keys for testing:
 * admin-token-12345 -> ADMIN, analyst-token-67890 -> ANALYST
 */
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter implements Ordered {

    private static final int ORDER = -100;

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("X-API-Key");
        if (token == null || token.isBlank()) {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                token = auth.substring(7).trim();
            }
        }
        if (token != null && !token.isBlank()) {
            if ("admin-token-12345".equals(token)) {
                setAuthentication("admin", "ROLE_ADMIN");
            } else if ("analyst-token-67890".equals(token)) {
                setAuthentication("analyst", "ROLE_ANALYST");
            }
        }
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String principal, String role) {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority(role))
            )
        );
    }
}
