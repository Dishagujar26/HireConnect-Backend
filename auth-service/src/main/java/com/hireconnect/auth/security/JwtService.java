package com.hireconnect.auth.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hireconnect.auth.entity.UserCredential;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Service for handling JSON Web Tokens (JWT).
 * Provides methods for generating, parsing, and validating JWT tokens.
 * @author Disha Gujar
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a JWT token for the given user.
     * 
     * @param user the user credentials
     * @return a signed JWT token string
     
 * @author Disha Gujar
 */
    public String generateToken(UserCredential user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getUserId())
                .claim("role", user.getRole().name())
                .claim("provider", user.getProvider().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey())
                .compact();
    }

    /**
     * Extracts the email (subject) from the JWT token.
     * 
     * @param token the JWT token
     * @return the email subject
     
 * @author Disha Gujar
 */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the user ID from the JWT token.
     * 
     * @param token the JWT token
     * @return the user ID
     
 * @author Disha Gujar
 */
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    /**
     * Extracts the user role from the JWT token.
     * 
     * @param token the JWT token
     * @return the role name
     
 * @author Disha Gujar
 */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Validates if the JWT token is not expired.
     * 
     * @param token the JWT token
     * @return true if valid, false otherwise
     
 * @author Disha Gujar
 */
    public boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}