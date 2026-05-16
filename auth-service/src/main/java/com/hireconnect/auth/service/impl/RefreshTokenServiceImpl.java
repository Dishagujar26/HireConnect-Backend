package com.hireconnect.auth.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hireconnect.auth.entity.RefreshToken;
import com.hireconnect.auth.entity.UserCredential;
import com.hireconnect.auth.repository.RefreshTokenRepository;
import com.hireconnect.auth.service.RefreshTokenService;

import org.springframework.data.redis.core.StringRedisTemplate;

import lombok.RequiredArgsConstructor;
/**
 * Implementation of the business logic for RefreshToken service.
 *
 * @author Disha Gujar
 */

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenServiceImpl.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    // Redis key prefix for namespacing
    private static final String REDIS_REFRESH_PREFIX = "hireconnect:auth:refresh:";

    /**
     * Creates or updates a refresh token.
     * Logic: Saves to Redis for performance (with TTL) and MySQL for persistence.
     */
    @Override
    public RefreshToken createOrUpdateRefreshToken(UserCredential user) {
        log.info("PERFORMANCE: Creating/Updating refresh token in Redis for userId: {}", user.getUserId());

        String tokenValue = UUID.randomUUID().toString();
        
        // 1. Prepare the RefreshToken object (JPA Entity)
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElse(RefreshToken.builder().user(user).build());

        refreshToken.setToken(tokenValue);
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000));

        // 2. PRIMARY STORAGE: Redis with TTL (Time To Live)
        // This ensures the token automatically disappears from Redis when it expires.
        try {
            String redisKey = REDIS_REFRESH_PREFIX + tokenValue;
            redisTemplate.opsForValue().set(
                redisKey, 
                String.valueOf(user.getUserId()), 
                java.time.Duration.ofMillis(refreshExpirationMs)
            );
            log.info("REDIS: Refresh token stored with TTL of {} ms", refreshExpirationMs);
        } catch (Exception e) {
            log.error("REDIS ERROR: Failed to save to Redis. Falling back to MySQL only: {}", e.getMessage());
        }

        // 3. SECONDARY STORAGE: MySQL (Keeping for consistency and return type)
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.info("MYSQL: Refresh token metadata updated for userId: {}", user.getUserId());

        return savedToken;
    }

    /**
     * Verifies the refresh token.
     * Logic: Checks Redis first (Fast path). Falls back to MySQL if Redis is down.
     */
    @Override
    public RefreshToken verifyRefreshToken(String token) {
        log.info("PERFORMANCE: Verifying refresh token using Redis-first strategy");

        // 1. FAST PATH: Check Redis
        String redisKey = REDIS_REFRESH_PREFIX + token;
        try {
            String userId = redisTemplate.opsForValue().get(redisKey);
            if (userId != null) {
                log.info("REDIS HIT: Valid token found for userId: {}", userId);
                // Even if found in Redis, we fetch the full entity from DB to return the expected object
                return refreshTokenRepository.findByToken(token)
                        .orElseThrow(() -> new RuntimeException("Token data inconsistent in Database"));
            }
        } catch (Exception e) {
            log.warn("REDIS WARNING: Redis lookup failed, falling back to MySQL: {}", e.getMessage());
        }

        // 2. FALLBACK PATH: Check MySQL (Standard logic)
        log.info("MYSQL: Checking database for token verification");
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("VERIFICATION FAILED: Invalid refresh token provided");
                    return new RuntimeException("Invalid refresh token");
                });

        // Manual expiry check for MySQL fallback
        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("EXPIRED: Token expired in MySQL. Deleting...");
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        log.info("VERIFICATION SUCCESS: Token validated via MySQL fallback");
        return refreshToken;
    }
}
