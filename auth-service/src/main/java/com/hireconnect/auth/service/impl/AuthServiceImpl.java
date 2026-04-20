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
import com.hireconnect.auth.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;

// [Disha Gujar] : Service implementation for core authentication and security operations.
// Handles user registration, JWT-based login, token validation, and secure password recovery.
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
//    private final NotificationServiceClient notificationServiceClient;
    private final NotificationEventProducer notificationEventProducer;

    @Value("${auth.reset-otp-expiration-minutes}")
    private long resetOtpExpirationMinutes;

    // [Disha Gujar] : Registers a new user (LOCAL provider) and generates initial access/refresh tokens.
    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration started for email: {}, role: {}", request.getEmail(), request.getRole());

        if (authRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed because email is already registered: {}", request.getEmail());
            throw new RuntimeException("Email is already registered");
        }

        UserCredential user = UserCredential.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .provider(AuthProvider.LOCAL)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        UserCredential savedUser = authRepository.save(user);
        log.info("User registered successfully with userId: {}, email: {}, role: {}",
                savedUser.getUserId(), savedUser.getEmail(), savedUser.getRole());

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

    // [Disha Gujar] : Validates user credentials and issues fresh JWT and Refresh tokens upon success.
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

    // [Disha Gujar] : Exchanges a valid refresh token for a new access token to maintain session continuity.
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

    // [Disha Gujar] : Generates a secure OTP for password recovery and dispatches it via the Notification Service.
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
        log.info("Existing OTP entries deleted for email: {}", user.getEmail());

        String otp = String.valueOf((int) ((Math.random() * 900000) + 100000));

        PasswordResetOtp passwordResetOtp = PasswordResetOtp.builder()
                .email(user.getEmail())
                .otp(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(resetOtpExpirationMinutes))
                .used(false)
                .build();

        passwordResetOtpRepository.save(passwordResetOtp);
        log.info("New password reset OTP generated and saved for userId: {}, email: {}",
                user.getUserId(), user.getEmail());

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

//        notificationServiceClient.createNotification(
//                String.valueOf(user.getUserId()),
//                user.getEmail(),
//                user.getRole().name(),
//                NotificationCreateRequestDto.builder()
//                        .recipientUserId(user.getUserId())
//                        .recipientEmail(user.getEmail())
//                        .title("Password Reset OTP")
//                        .message(message)
//                        .type(NotificationType.SYSTEM)
//                        .sendEmail(true)
//                        .build()
//        );
        
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

    // [Disha Gujar] : Finalizes the password reset process by verifying the OTP and updating the user's password hash.
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Reset password flow started for email: {}", request.getEmail());

        UserCredential user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Reset password failed. No account found for email: {}", request.getEmail());
                    return new RuntimeException("No account found with this email");
                });

        PasswordResetOtp passwordResetOtp = passwordResetOtpRepository
                .findTopByEmailAndOtpAndUsedFalseOrderByIdDesc(request.getEmail(), request.getOtp())
                .orElseThrow(() -> {
                    log.warn("Reset password failed due to invalid OTP for email: {}", request.getEmail());
                    return new RuntimeException("Invalid OTP");
                });

        if (passwordResetOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            log.warn("Reset password failed because OTP expired for email: {}", request.getEmail());
            throw new RuntimeException("OTP has expired");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        authRepository.save(user);

        passwordResetOtp.setUsed(true);
        passwordResetOtpRepository.save(passwordResetOtp);

        log.info("Password reset successful for userId: {}, email: {}", user.getUserId(), user.getEmail());
    }

    // [Disha Gujar] : Decodes and validates a JWT token, returning user details for gateway authorization checks.
    @Override
    public TokenValidationResponse validateToken(String token) {
        log.info("Token validation started");

        try {
            if (token == null || token.isBlank()) {
                log.warn("Token validation failed because token is missing");
                throw new RuntimeException("Token is missing");
            }

            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            boolean valid = jwtService.isTokenValid(token);

            if (!valid) {
                log.warn("Token validation failed because token is invalid or expired");
                throw new RuntimeException("Invalid or expired token");
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
            log.warn("Token validation failed: {}", e.getMessage());
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Invalid or expired token")
                    .build();
        }
    }
}
