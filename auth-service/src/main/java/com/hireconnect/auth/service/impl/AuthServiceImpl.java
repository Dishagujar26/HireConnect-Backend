package com.hireconnect.auth.service.impl;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hireconnect.auth.dto.request.ForgotPasswordRequest;
import com.hireconnect.auth.dto.request.LoginRequest;
import com.hireconnect.auth.dto.request.RefreshTokenRequest;
import com.hireconnect.auth.dto.request.RegisterRequest;
import com.hireconnect.auth.dto.request.ResetPasswordRequest;
import com.hireconnect.auth.dto.response.AuthResponse;
import com.hireconnect.auth.dto.response.TokenValidationResponse;
import com.hireconnect.auth.entity.AuthProvider;
import com.hireconnect.auth.entity.NotificationType;
import com.hireconnect.auth.entity.PasswordResetOtp;
import com.hireconnect.auth.entity.RefreshToken;
import com.hireconnect.auth.entity.Role;
import com.hireconnect.auth.entity.UserCredential;
import com.hireconnect.auth.event.NotificationEvent;
import com.hireconnect.auth.producer.NotificationEventProducer;
import com.hireconnect.auth.repository.AuthRepository;
import com.hireconnect.auth.repository.PasswordResetOtpRepository;
import com.hireconnect.auth.security.JwtService;
import com.hireconnect.auth.service.AuthService;
import com.hireconnect.auth.service.BloomFilterService;
import com.hireconnect.auth.service.OtpService;
import com.hireconnect.auth.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of the AuthService.
 * Handles user registration, authentication, token management, and password recovery.
 * Integrates with Kafka for sending notifications.
 * @author Disha Gujar
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final NotificationEventProducer notificationEventProducer;
    private final BloomFilterService bloomFilterService;
    private final OtpService otpService;

    @Value("${auth.reset-otp-expiration-minutes}")
    private long resetOtpExpirationMinutes;

    @Value("${auth.admin-email}")
    private String adminEmail;

    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration started for email: {}, role: {}", request.getEmail(), request.getRole());

        // Bloom Filter Check (Fast Layer)
        if (bloomFilterService.mightContainEmail(request.getEmail())) {
            log.info("Email possibly exists in Bloom Filter, performing DB check for: {}", request.getEmail());
            // Double check with DB
            if (authRepository.existsByEmail(request.getEmail())) {
                log.warn("Registration failed because email is already registered: {}", request.getEmail());
                throw new RuntimeException("Email is already registered");
            }
        }

        // [Admin Identity Guard] — If the registering email matches the designated
        // admin email, forcefully assign ROLE_ADMIN regardless of the submitted role.
        // This ensures only one Super Admin can exist on the platform.
        Role assignedRole = request.getEmail().equalsIgnoreCase(adminEmail)
                ? Role.ADMIN
                : request.getRole();

        if (assignedRole == Role.ADMIN) {
            log.info("Admin identity detected for email: {}. Assigning ROLE_ADMIN.", request.getEmail());
        }

        UserCredential user = UserCredential.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(assignedRole)
                .provider(AuthProvider.LOCAL)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        UserCredential savedUser = authRepository.save(user);
        log.info("User registered successfully with userId: {}, email: {}, role: {}",
                savedUser.getUserId(), savedUser.getEmail(), savedUser.getRole());

        // Update Bloom Filter after successful registration
        bloomFilterService.addEmail(savedUser.getEmail());

        String accessToken = jwtService.generateToken(savedUser);
        RefreshToken refreshToken = refreshTokenService.createOrUpdateRefreshToken(savedUser);

        return AuthResponse.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .message(savedUser.getRole() + " registered successfully")
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login started for email: {}", request.getEmail());

        UserCredential user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed. User not found for email: {}", request.getEmail());
                    return new RuntimeException("Invalid email or password");
                });

        if (!user.getIsActive()) {
            log.warn("Login failed because account is deactivated for userId: {}, email: {}",
                    user.getUserId(), user.getEmail());
            throw new RuntimeException("Account is deactivated");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed due to invalid password for userId: {}, email: {}",
                    user.getUserId(), user.getEmail());
            throw new RuntimeException("Invalid email or password");
        }

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createOrUpdateRefreshToken(user);

        log.info("Login successful for userId: {}, email: {}, role: {}",
                user.getUserId(), user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .message("Login successful")
                .build();
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refresh token flow started");

        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
        UserCredential user = refreshToken.getUser();

        if (!user.getIsActive()) {
            log.warn("Refresh token failed because account is deactivated for userId: {}, email: {}",
                    user.getUserId(), user.getEmail());
            throw new RuntimeException("Account is deactivated");
        }

        String accessToken = jwtService.generateToken(user);

        log.info("Access token refreshed successfully for userId: {}, email: {}",
                user.getUserId(), user.getEmail());

        return AuthResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .message("Access token refreshed successfully")
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Forgot password flow started for email: {}", request.getEmail());

        UserCredential user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Forgot password failed. No account found for email: {}", request.getEmail());
                    return new RuntimeException("No account found with this email");
                });

        passwordResetOtpRepository.deleteByEmail(user.getEmail());
        log.info("MYSQL CLEANUP: Old OTP metadata removed for {}", user.getEmail());

        // 1. GENERATE & STORE IN REDIS (New fast path)
        String otp = otpService.generateAndStoreOtp(user.getEmail());

        // 2. BACKUP STORAGE (Optional: keeping MySQL record for audit trail if desired)
        PasswordResetOtp passwordResetOtp = PasswordResetOtp.builder()
                .email(user.getEmail())
                .otp(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(resetOtpExpirationMinutes))
                .used(false)
                .build();
        passwordResetOtpRepository.save(passwordResetOtp);
        
        log.info("REDIS & MYSQL: New password reset OTP generated for userId: {}", user.getUserId());

        String message =
                "Dear User,\n\n"
                + "Greetings from HireConnect.\n\n"
                + "We received a request to reset your account password.\n\n"
                + "Your OTP for password reset is: " + otp + "\n"
                + "This OTP is valid for " + resetOtpExpirationMinutes + " minutes.\n\n"
                + "If you did not request this, please ignore this email.\n\n"
                + "Regards,\n"
                + "Support Team\n"
                + "HireConnect";
        
        NotificationEvent event = NotificationEvent.builder()
                .recipientUserId(user.getUserId())
                .recipientEmail(user.getEmail())
                .title("Password Reset OTP")
                .message(message)
                .type(NotificationType.SYSTEM)
                .sendEmail(true)
                .build();
        
        notificationEventProducer.sendNotification(event);

        log.info("Password reset notification request sent successfully for userId: {}, email: {}",
                user.getUserId(), user.getEmail());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Reset password flow started for email: {}", request.getEmail());

        UserCredential user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Reset password failed. No account found for email: {}", request.getEmail());
                    return new RuntimeException("No account found with this email");
                });

        // 1. FAST PATH: Check Redis first
        boolean isValid = otpService.validateOtp(request.getEmail(), request.getOtp());
        
        if (!isValid) {
            log.warn("VERIFICATION FAILED: OTP either expired in Redis or is incorrect for {}", request.getEmail());
            // Fallback check in MySQL to ensure we don't break existing tokens during migration
            PasswordResetOtp backupOtp = passwordResetOtpRepository
                .findTopByEmailAndOtpAndUsedFalseOrderByIdDesc(request.getEmail(), request.getOtp())
                .orElseThrow(() -> new RuntimeException("Invalid OTP (Verified via Redis & DB)"));

            if (backupOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
                log.warn("EXPIRED: Backup OTP in MySQL has also expired");
                throw new RuntimeException("OTP has expired");
            }
            log.info("FALLBACK SUCCESS: Validated via MySQL backup");
        } else {
            log.info("REDIS HIT: OTP validated successfully for {}", request.getEmail());
        }

        // 2. APPLY PASSWORD CHANGE
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        authRepository.save(user);

        // 3. CLEANUP
        otpService.deleteOtp(request.getEmail());
        passwordResetOtpRepository.deleteByEmail(request.getEmail());

        log.info("Password reset successful for userId: {}, email: {}", user.getUserId(), user.getEmail());
    }

    @Override
    public TokenValidationResponse validateToken(String token) {
        log.info("Token validation started");

        if (token == null || token.isBlank()) {
            log.warn("Token validation failed because token is missing");
                        return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Token is missing")
                    .build();

        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            boolean valid = jwtService.isTokenValid(token);

            if (!valid) {
                log.warn("Token validation failed because token is invalid or expired");
                return TokenValidationResponse.builder()
                        .valid(false)
                        .message("Invalid or expired token")
                        .build();

            }

            Long userId = jwtService.extractUserId(token);
            String email = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);

            log.info("Token validation successful for userId: {}, email: {}, role: {}", userId, email, role);

            return TokenValidationResponse.builder()
                    .valid(true)
                    .userId(userId)
                    .email(email)
                    .role(Enum.valueOf(Role.class, role))
                    .message("Token is valid")
                    .build();
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Token validation failed: " + e.getMessage())
                    .build();
        }
    }
}
