package com.hireconnect.auth.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.hireconnect.auth.entity.RefreshToken;
import com.hireconnect.auth.entity.UserCredential;
import com.hireconnect.auth.repository.RefreshTokenRepository;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private UserCredential user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        user = new UserCredential();
        user.setUserId(1L);
        user.setEmail("test@example.com");

        refreshToken = RefreshToken.builder()
                .id(1L)
                .user(user)
                .token("old-token")
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();

        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationMs", 86400000L);
    }

    @Test
    void createOrUpdateRefreshToken_NewToken_Success() {
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        RefreshToken result = refreshTokenService.createOrUpdateRefreshToken(user);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertNotNull(result.getToken());
        verify(valueOperations).set(anyString(), anyString(), any(java.time.Duration.class));
    }

    @Test
    void createOrUpdateRefreshToken_RedisError_FallsBackToMysql() {
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis down"));

        RefreshToken result = refreshTokenService.createOrUpdateRefreshToken(user);

        assertNotNull(result);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void verifyRefreshToken_RedisHit_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("1");
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(refreshToken));

        RefreshToken result = refreshTokenService.verifyRefreshToken("valid-token");

        assertNotNull(result);
        assertEquals(refreshToken, result);
        verify(valueOperations).get(anyString());
    }

    @Test
    void verifyRefreshToken_RedisMiss_FallbackToMysql_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(refreshToken));

        RefreshToken result = refreshTokenService.verifyRefreshToken("valid-token");

        assertNotNull(result);
        assertEquals(refreshToken, result);
    }

    @Test
    void verifyRefreshToken_RedisError_FallbackToMysql_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis error"));
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(refreshToken));

        RefreshToken result = refreshTokenService.verifyRefreshToken("valid-token");

        assertNotNull(result);
        assertEquals(refreshToken, result);
    }

    @Test
    void createOrUpdateRefreshToken_ExistingToken_Success() {
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken result = refreshTokenService.createOrUpdateRefreshToken(user);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertNotEquals("old-token", result.getToken());
    }

    @Test
    void verifyRefreshToken_RedisMiss() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(refreshToken));

        RefreshToken result = refreshTokenService.verifyRefreshToken("valid-token");

        assertNotNull(result);
        assertEquals(refreshToken, result);
    }

    @Test
    void verifyRefreshToken_Success() {
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(refreshToken));

        RefreshToken result = refreshTokenService.verifyRefreshToken("valid-token");

        assertNotNull(result);
        assertEquals(refreshToken, result);
    }

    @Test
    void verifyRefreshToken_InvalidToken_ThrowsException() {
        when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> refreshTokenService.verifyRefreshToken("invalid-token"));
    }

    @Test
    void verifyRefreshToken_ExpiredToken_ThrowsException() {
        refreshToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(refreshToken));

        assertThrows(RuntimeException.class, () -> refreshTokenService.verifyRefreshToken("expired-token"));
        verify(refreshTokenRepository).delete(refreshToken);
    }
}
