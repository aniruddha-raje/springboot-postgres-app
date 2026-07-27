package com.example.springboot_postgres.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Minimal auth filter for the CRUD APIs. It only checks that the request
 * carries a standard {@code Authorization: Bearer <token>} header whose token
 * is shaped like a JWT (three non-empty base64url segments). It intentionally
 * does <b>not</b> verify the signature or claims — any structurally valid JWT
 * is accepted. Requests without such a header get {@code 401 Unauthorized}.
 *
 * <p>The filter is scoped to specific URL patterns via
 * {@link SecurityConfig}, so health/version/actuator/Swagger stay open.
 */
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    /** A base64url segment: JWT parts use [A-Za-z0-9_-], no padding. */
    private static final Pattern JWT_PATTERN =
            Pattern.compile("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            unauthorized(response, "Missing or malformed Authorization header");
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (!JWT_PATTERN.matcher(token).matches()) {
            unauthorized(response, "Authorization token is not a valid JWT");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        log.debug("Rejecting request: {}", message);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}");
    }
}
