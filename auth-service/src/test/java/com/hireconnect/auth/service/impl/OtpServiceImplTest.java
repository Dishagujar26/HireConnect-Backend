package com.hireconnect.auth.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private OtpServiceImpl otpService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(otpService, "otpExpirationMinutes", 10L);
    }

    @Test
    void generateAndStoreOtp_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String otp = otpService.generateAndStoreOtp("test@example.com");

        assertNotNull(otp);
        assertEquals(6, otp.length());
        verify(valueOperations).set(startsWith("hireconnect:auth:otp:"), eq(otp), eq(10L), eq(TimeUnit.MINUTES));
    }

    @Test
    void generateAndStoreOtp_RedisError_StillReturnsOtp() {
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis down"));

        String otp = otpService.generateAndStoreOtp("test@example.com");

        assertNotNull(otp);
        assertEquals(6, otp.length());
    }

    @Test
    void validateOtp_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("123456");

        boolean isValid = otpService.validateOtp("test@example.com", "123456");

        assertTrue(isValid);
    }

    @Test
    void validateOtp_WrongOtp_ReturnsFalse() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("123456");

        boolean isValid = otpService.validateOtp("test@example.com", "654321");

        assertFalse(isValid);
    }

    @Test
    void validateOtp_ExpiredOrNotFound_ReturnsFalse() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        boolean isValid = otpService.validateOtp("test@example.com", "123456");

        assertFalse(isValid);
    }

    @Test
    void validateOtp_RedisError_ReturnsFalse() {
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis down"));

        boolean isValid = otpService.validateOtp("test@example.com", "123456");

        assertFalse(isValid);
    }

    @Test
    void deleteOtp_Success() {
        otpService.deleteOtp("test@example.com");

        verify(redisTemplate).delete(startsWith("hireconnect:auth:otp:"));
    }

    @Test
    void deleteOtp_RedisError_DoesNotThrow() {
        when(redisTemplate.delete(anyString())).thenThrow(new RuntimeException("Redis down"));

        assertDoesNotThrow(() -> otpService.deleteOtp("test@example.com"));
    }

    @Test
    void generateAndStoreOtp_CustomExpiration_Success() {
        ReflectionTestUtils.setField(otpService, "otpExpirationMinutes", 20L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String otp = otpService.generateAndStoreOtp("test@example.com");

        assertNotNull(otp);
        verify(valueOperations).set(eq("hireconnect:auth:otp:test@example.com"), eq(otp), eq(20L), eq(TimeUnit.MINUTES));
    }
}

