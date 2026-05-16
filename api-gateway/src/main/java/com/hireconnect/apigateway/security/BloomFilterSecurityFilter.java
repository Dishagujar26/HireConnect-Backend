package com.hireconnect.apigateway.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class BloomFilterSecurityFilter extends OncePerRequestFilter {

    private final BloomFilterSecurityService bloomFilterSecurityService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String ip = request.getRemoteAddr();
        String path = request.getRequestURI();

        // 1. Check IP Blacklist (Bloom Filter check)
        // This is O(1) constant time. Even with 1 million blocked IPs, it's instant.
        if (bloomFilterSecurityService.isIpBlacklisted(ip)) {
            log.warn("SECURITY ALERT: Blocked request from blacklisted IP: {}", ip);
            writeForbidden(response, "Access Denied: Your IP has been flagged for suspicious activity.");
            return;
        }

        // 2. Rate Limiting Check (Skip for Actuator/Internal health checks)
        boolean isActuatorPath = path.contains("/actuator");
        if (!isActuatorPath && bloomFilterSecurityService.isRateLimited(ip)) {
            response.setStatus(429); // 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Too many requests. Please try again in a minute.\"}");
            return;
        }

        // 3. Validate Path & Check for Malicious Scanners
        boolean isWhitelisted = bloomFilterSecurityService.isValidPath(path);
        boolean isStandardPath = path.startsWith("/api/") || path.startsWith("/actuator/") || path.startsWith("/swagger") || path.contains("api-docs");
        
        if (isSuspiciousPath(path)) {
            log.error("HACKER TRAP TRIGGERED: IP {} attempted to access sensitive path {}. Blacklisting...", ip, path);
            bloomFilterSecurityService.blacklistIp(ip); // Permanently block this IP in Redis
            writeForbidden(response, "Security Violation: Your IP has been blacklisted.");
            return;
        }

        // Only block if it's NOT a standard path AND NOT in the Bloom Filter
        if (!isStandardPath && !isWhitelisted && isStrictSecurityMode(path)) {
            log.warn("SECURITY WARNING: Invalid path access attempt: {} from IP: {}", path, ip);
            writeForbidden(response, "Access Denied: Invalid Path");
            return;
        }

        // 4. Request is safe, proceed to the next filter
        filterChain.doFilter(request, response);
    }

    /**
     * Determines if a path is commonly used by automated exploit scanners.
     */
    private boolean isSuspiciousPath(String path) {
        String lowerPath = path.toLowerCase();
        return lowerPath.endsWith(".php") ||    // No PHP files in this Java project
               lowerPath.endsWith(".asp") ||    // No ASP files
               lowerPath.contains("/.env") ||   // Attempting to steal secrets
               lowerPath.contains("/.git") ||   // Attempting to steal source code
               lowerPath.contains("/admin/config") || 
               lowerPath.contains("/etc/passwd"); // Linux password file injection attempt
    }

    /**
     * Helper to determine if we should be strict about non-whitelisted paths.
     * We allow root and swagger by default.
     */
    private boolean isStrictSecurityMode(String path) {
        return !path.equals("/") && !path.contains("swagger") && !path.contains("api-docs") && !path.contains("actuator");
    }

    private void writeForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
