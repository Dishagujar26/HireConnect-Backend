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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenServiceImpl.class);

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    @Override
    public RefreshToken createOrUpdateRefreshToken(UserCredential user) {
        log.info("Creating or updating refresh token for userId: {}, email: {}",
                user.getUserId(), user.getEmail());

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElse(
                        RefreshToken.builder()
                                .user(user)
                                .build()
                );

        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000));

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.info("Refresh token saved successfully for userId: {}", user.getUserId());

        return savedToken;
    }

    @Override
    public RefreshToken verifyRefreshToken(String token) {
        log.info("Refresh token verification started");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Refresh token verification failed because token is invalid");
                    return new RuntimeException("Invalid refresh token");
                });

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Refresh token expired for userId: {}. Deleting expired token.",
                    refreshToken.getUser().getUserId());
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        log.info("Refresh token verified successfully for userId: {}",
                refreshToken.getUser().getUserId());

        return refreshToken;
    }
}
