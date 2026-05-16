package com.hireconnect.auth.service.impl;

import com.hireconnect.auth.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final StringRedisTemplate redisTemplate;

    @Value("${auth.reset-otp-expiration-minutes:10}")
    private long otpExpirationMinutes;

    private static final String REDIS_OTP_PREFIX = "hireconnect:auth:otp:";

    @Override
    public String generateAndStoreOtp(String email) {
        String otp = String.valueOf((int) ((Math.random() * 900000) + 100000));
        String redisKey = REDIS_OTP_PREFIX + email;

        log.info("PERFORMANCE: Storing OTP in Redis for email: {} with {} min TTL", email, otpExpirationMinutes);

        try {
            redisTemplate.opsForValue().set(redisKey, otp, otpExpirationMinutes, TimeUnit.MINUTES);
            log.info("REDIS: OTP successfully stored for {}", email);
        } catch (Exception e) {
            log.error("REDIS ERROR: Failed to store OTP in Redis: {}", e.getMessage());
            // In a real production app, you might fall back to DB here if needed.
        }

        return otp;
    }

    @Override
    public boolean validateOtp(String email, String otp) {
        String redisKey = REDIS_OTP_PREFIX + email;
        log.info("PERFORMANCE: Validating OTP from Redis for email: {}", email);

        try {
            String storedOtp = redisTemplate.opsForValue().get(redisKey);
            if (storedOtp == null) {
                log.warn("REDIS MISS: OTP expired or not found for {}", email);
                return false;
            }
            return storedOtp.equals(otp);
        } catch (Exception e) {
            log.error("REDIS ERROR: OTP validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void deleteOtp(String email) {
        String redisKey = REDIS_OTP_PREFIX + email;
        log.info("REDIS: Deleting used OTP for {}", email);
        try {
            redisTemplate.delete(redisKey);
        } catch (Exception e) {
            log.warn("REDIS WARNING: Failed to delete OTP key: {}", e.getMessage());
        }
    }
}
