package com.hireconnect.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class BloomFilterService {

    private final StringRedisTemplate redisTemplate;
    private static final String EMAIL_FILTER = "hireconnect:emails";

    /**
     * Adds an email to the Bloom Filter.
     */
    public void addEmail(String email) {
        try {
            // BF.ADD key item
            redisTemplate.execute(new DefaultRedisScript<>("return redis.call('BF.ADD', KEYS[1], ARGV[1])", Long.class),
                    Collections.singletonList(EMAIL_FILTER), email);
        } catch (Exception e) {
            log.warn("Failed to add email to Bloom Filter: {}. System will fall back to DB.", e.getMessage());
        }
    }

    /**
     * Checks if an email might exist in the Bloom Filter.
     * Returns false if definitely not present.
     * Returns true if possibly present.
     */
    public boolean mightContainEmail(String email) {
        try {
            // BF.EXISTS key item
            Long result = redisTemplate.execute(new DefaultRedisScript<>("return redis.call('BF.EXISTS', KEYS[1], ARGV[1])", Long.class),
                    Collections.singletonList(EMAIL_FILTER), email);
            return result != null && result == 1L;
        } catch (Exception e) {
            log.warn("Bloom Filter check failed: {}. Falling back to true (safe mode).", e.getMessage());
            return true; // Fallback to true so the system proceeds to check the DB
        }
    }
}
