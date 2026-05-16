package com.hireconnect.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

@ExtendWith(MockitoExtension.class)
class BloomFilterServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private BloomFilterService bloomFilterService;

    @Test
    void addEmail_Success() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any())).thenReturn(1L);

        bloomFilterService.addEmail("test@example.com");

        verify(redisTemplate).execute(any(RedisScript.class), eq(Collections.singletonList("hireconnect:emails")), eq("test@example.com"));
    }

    @Test
    void addEmail_Error_DoesNotThrow() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any())).thenThrow(new RuntimeException("Redis error"));

        assertDoesNotThrow(() -> bloomFilterService.addEmail("test@example.com"));
    }

    @Test
    void mightContainEmail_ReturnsTrue() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any())).thenReturn(1L);

        boolean result = bloomFilterService.mightContainEmail("test@example.com");

        assertTrue(result);
    }

    @Test
    void mightContainEmail_ReturnsFalse() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any())).thenReturn(0L);

        boolean result = bloomFilterService.mightContainEmail("test@example.com");

        assertFalse(result);
    }

    @Test
    void mightContainEmail_Error_ReturnsTrueFallback() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any())).thenThrow(new RuntimeException("Redis error"));

        boolean result = bloomFilterService.mightContainEmail("test@example.com");

        assertTrue(result);
    }
}
