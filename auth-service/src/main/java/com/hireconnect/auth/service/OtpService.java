package com.hireconnect.auth.service;

public interface OtpService {
    /**
     * Generates and stores a 6-digit OTP in Redis with a TTL.
     */
    String generateAndStoreOtp(String email);

    /**
     * Validates the OTP against the value stored in Redis.
     */
    boolean validateOtp(String email, String otp);

    /**
     * Deletes the OTP after it has been used.
     */
    void deleteOtp(String email);
}
