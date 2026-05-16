package com.hireconnect.apigateway.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
/**
 * Service interface defining the contract for Jwt business logic.
 *
 * @author Disha Gujar
 */

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}") 
    private String secret;

    private SecretKey getSignKey() {
        log.debug("Generating JWT signing key for token validation");
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    /**
     * Extract all claims.
     *
     * @author Disha Gujar
     */

    public Claims extractAllClaims(String token) {
        log.debug("Extracting all claims from JWT token");
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    /**
     * Extract email.
     *
     * @author Disha Gujar
     */

    public String extractEmail(String token) {
        String email = extractAllClaims(token).getSubject();
        log.debug("Extracted email from token: {}", email);
        return email;
    }
    /**
     * Extract user id.
     *
     * @author Disha Gujar
     */

    public Long extractUserId(String token) {
        Long userId = extractAllClaims(token).get("userId", Long.class);
        log.debug("Extracted userId from token: {}", userId);
        return userId;
    }
    /**
     * Extract role.
     *
     * @author Disha Gujar
     */

    public String extractRole(String token) {
        String role = extractAllClaims(token).get("role", String.class);
        log.debug("Extracted role from token: {}", role);
        return role;
    }
    /**
     * Checks if token valid.
     *
     * @author Disha Gujar
     */

    public boolean isTokenValid(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        boolean isValid = expiration != null && expiration.after(new Date());
        log.debug("Token validation result: {}", isValid);
        return isValid;
    }
}
