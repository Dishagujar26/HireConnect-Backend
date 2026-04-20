package com.hireconnect.apigateway.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class GatewayJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final PublicEndpointService publicEndpointService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        boolean isPublic = publicEndpointService.isPublic(request);
        if (isPublic) {
            log.debug("Skipping JWT filter for public endpoint: {} {}", request.getMethod(), request.getRequestURI());
        }
        return isPublic;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        log.info("Processing gateway request: {} {}", method, requestPath);

        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.debug("Allowing OPTIONS request without JWT validation for path: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Unauthorized request to {} {} - missing or invalid Authorization header", method, requestPath);
            writeUnauthorized(response, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtService.isTokenValid(token)) {
                log.warn("Unauthorized request to {} {} - token is invalid or expired", method, requestPath);
                writeUnauthorized(response, "Invalid or expired token");
                return;
            }

            Long userId = jwtService.extractUserId(token);
            String email = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);

            log.info("JWT validated successfully for userId: {}, email: {}, role: {}, path: {}",
                    userId, email, role, requestPath);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            AuthHeaderRequestWrapper wrappedRequest =
                    new AuthHeaderRequestWrapper(request, userId, email, role);

            filterChain.doFilter(wrappedRequest, response);
            log.debug("Request forwarded successfully to downstream service: {} {}", method, requestPath);

        } catch (Exception ex) {
            log.error("Token validation failed for {} {}: {}", method, requestPath, ex.getMessage(), ex);
            writeUnauthorized(response, "Token validation failed");
        } finally {
            SecurityContextHolder.clearContext();
            log.debug("Security context cleared for request: {} {}", method, requestPath);
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
