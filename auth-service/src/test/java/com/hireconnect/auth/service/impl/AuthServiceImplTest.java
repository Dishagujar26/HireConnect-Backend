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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.hireconnect.auth.dto.request.LoginRequest;
import com.hireconnect.auth.dto.request.RefreshTokenRequest;
import com.hireconnect.auth.dto.request.RegisterRequest;
import com.hireconnect.auth.dto.request.ForgotPasswordRequest;
import com.hireconnect.auth.dto.request.ResetPasswordRequest;
import com.hireconnect.auth.dto.response.AuthResponse;
import com.hireconnect.auth.dto.response.TokenValidationResponse;
import com.hireconnect.auth.entity.PasswordResetOtp;
import com.hireconnect.auth.entity.RefreshToken;
import com.hireconnect.auth.entity.Role;
import com.hireconnect.auth.entity.UserCredential;
import com.hireconnect.auth.producer.NotificationEventProducer;
import com.hireconnect.auth.repository.AuthRepository;
import com.hireconnect.auth.repository.PasswordResetOtpRepository;
import com.hireconnect.auth.security.JwtService;
import com.hireconnect.auth.service.BloomFilterService;
import com.hireconnect.auth.service.OtpService;
import com.hireconnect.auth.service.RefreshTokenService;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private NotificationEventProducer notificationEventProducer;

    @Mock
    private PasswordResetOtpRepository passwordResetOtpRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private BloomFilterService bloomFilterService;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserCredential user;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        user = new UserCredential();
        user.setUserId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");
        user.setRole(Role.CANDIDATE);
        user.setIsActive(true);

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password");
        registerRequest.setRole(Role.CANDIDATE);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        refreshToken = new RefreshToken();
        refreshToken.setToken("refresh_token_string");
        refreshToken.setUser(user);
    }

   
    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        when(bloomFilterService.mightContainEmail("test@example.com")).thenReturn(true);
        when(authRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
    }

    @Test
    void register_BloomFilterHit_DbMiss_Success() {
        when(bloomFilterService.mightContainEmail("test@example.com")).thenReturn(true);
        when(authRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashed");
        when(authRepository.save(any())).thenReturn(user);
        when(jwtService.generateToken(any())).thenReturn("token");
        when(refreshTokenService.createOrUpdateRefreshToken(any())).thenReturn(refreshToken);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        verify(authRepository).save(any());
        verify(bloomFilterService).addEmail("test@example.com");
    }

    @Test
    void register_DefaultRole_Success() {
        registerRequest.setRole(null);
        when(bloomFilterService.mightContainEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashed");
        when(authRepository.save(any())).thenReturn(user);
        when(jwtService.generateToken(any())).thenReturn("token");
        when(refreshTokenService.createOrUpdateRefreshToken(any())).thenReturn(refreshToken);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
    }

    @Test
    void login_Success() {
        when(authRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("access_token_string");
        when(refreshTokenService.createOrUpdateRefreshToken(user)).thenReturn(refreshToken);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("access_token_string", response.getAccessToken());
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        when(authRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        when(authRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_AccountDeactivated_ThrowsException() {
        user.setIsActive(false);
        when(authRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    }

    @Test
    void refreshToken_Success() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh_token_string");

        when(refreshTokenService.verifyRefreshToken("refresh_token_string")).thenReturn(refreshToken);
        when(jwtService.generateToken(user)).thenReturn("new_access_token");

        AuthResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals("new_access_token", response.getAccessToken());
        assertEquals("refresh_token_string", response.getRefreshToken());
    }

    @Test
    void refreshToken_UserDeactivated_ThrowsException() {
        user.setIsActive(false);

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh_token_string");

        when(refreshTokenService.verifyRefreshToken("refresh_token_string")).thenReturn(refreshToken);

        assertThrows(RuntimeException.class, () -> authService.refreshToken(request));
    }

    @Test
    void forgotPassword_Success() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        when(authRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(otpService.generateAndStoreOtp("test@example.com")).thenReturn("123456");
        when(passwordResetOtpRepository.save(any())).thenReturn(null);
        ReflectionTestUtils.setField(authService, "resetOtpExpirationMinutes", 10L);

        authService.forgotPassword(request);

        verify(otpService).generateAndStoreOtp("test@example.com");
        verify(notificationEventProducer).sendNotification(any());
        verify(passwordResetOtpRepository).save(any());
    }

    @Test
    void forgotPassword_NotificationFailure_BubblesUp() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        when(authRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(otpService.generateAndStoreOtp("test@example.com")).thenReturn("123456");
        when(passwordResetOtpRepository.save(any())).thenReturn(null);
        ReflectionTestUtils.setField(authService, "resetOtpExpirationMinutes", 10L);
        doThrow(new RuntimeException("Kafka down")).when(notificationEventProducer).sendNotification(any());

        assertThrows(RuntimeException.class, () -> authService.forgotPassword(request));
    }

    @Test
    void forgotPassword_UserNotFound_ThrowsException() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        when(authRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.forgotPassword(request));
    }

    @Test
    void resetPassword_Success() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        request.setNewPassword("newPass");

        when(authRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(otpService.validateOtp("test@example.com", "123456")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("hashedNewPass");

        authService.resetPassword(request);

        verify(authRepository).save(user);
        verify(otpService).deleteOtp("test@example.com");
        assertEquals("hashedNewPass", user.getPasswordHash());
    }

    @Test
    void resetPassword_UserNotFound_ThrowsException() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@example.com");

        when(authRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.resetPassword(request));
    }

    @Test
    void resetPassword_InvalidOtp_ThrowsException() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");

        when(authRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(otpService.validateOtp("test@example.com", "123456")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.resetPassword(request));
    }

    @Test
    void validateToken_Success() {
        when(jwtService.isTokenValid("valid_token")).thenReturn(true);
        when(jwtService.extractUserId("valid_token")).thenReturn(1L);
        when(jwtService.extractEmail("valid_token")).thenReturn("test@example.com");
        when(jwtService.extractRole("valid_token")).thenReturn("CANDIDATE");

        TokenValidationResponse response = authService.validateToken("Bearer valid_token");

        assertTrue(response.isValid());
        assertEquals(1L, response.getUserId());
        assertEquals(Role.CANDIDATE, response.getRole());
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        when(jwtService.isTokenValid("invalid_token")).thenReturn(false);

        TokenValidationResponse response = authService.validateToken("invalid_token");

        assertFalse(response.isValid());
        assertEquals("Invalid or expired token", response.getMessage());
    }

    @Test
    void validateToken_Exception_ReturnsFalse() {
        when(jwtService.isTokenValid("token")).thenThrow(new RuntimeException("JWT error"));

        TokenValidationResponse response = authService.validateToken("Bearer token");

        assertFalse(response.isValid());
    }

    @Test
    void validateToken_TokenMissing_ReturnsFalse() {
        TokenValidationResponse response = authService.validateToken(null);
        assertFalse(response.isValid());
        assertEquals("Token is missing", response.getMessage());

        response = authService.validateToken("");
        assertFalse(response.isValid());
    }

    @Test
    void register_AdminEmail_ShouldAssignAdminRole() {
        ReflectionTestUtils.setField(authService, "adminEmail", "admin@hireconnect.com");
        registerRequest.setEmail("admin@hireconnect.com");
        
        when(bloomFilterService.mightContainEmail("admin@hireconnect.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(authRepository.save(any())).thenAnswer(i -> {
            UserCredential u = i.getArgument(0);
            u.setUserId(10L);
            return u;
        });
        when(jwtService.generateToken(any())).thenReturn("token");
        when(refreshTokenService.createOrUpdateRefreshToken(any())).thenReturn(RefreshToken.builder().token("rt").build());

        AuthResponse response = authService.register(registerRequest);

        assertEquals(Role.ADMIN, response.getRole());
    }

    @Test
    void resetPassword_RedisMiss_FallbackToDb_Success() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        request.setNewPassword("newPass");

        PasswordResetOtp backupOtp = PasswordResetOtp.builder()
                .email("test@example.com")
                .otp("123456")
                .expiryTime(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        when(authRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(otpService.validateOtp("test@example.com", "123456")).thenReturn(false);
        when(passwordResetOtpRepository.findTopByEmailAndOtpAndUsedFalseOrderByIdDesc("test@example.com", "123456"))
                .thenReturn(Optional.of(backupOtp));
        when(passwordEncoder.encode("newPass")).thenReturn("hashedNewPass");

        authService.resetPassword(request);

        verify(authRepository).save(user);
        verify(otpService).deleteOtp("test@example.com");
    }

    @Test
    void resetPassword_ExpiredBackupOtp_ThrowsException() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");

        PasswordResetOtp expiredOtp = PasswordResetOtp.builder()
                .email("test@example.com")
                .otp("123456")
                .expiryTime(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .build();

        when(authRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(otpService.validateOtp("test@example.com", "123456")).thenReturn(false);
        when(passwordResetOtpRepository.findTopByEmailAndOtpAndUsedFalseOrderByIdDesc("test@example.com", "123456"))
                .thenReturn(Optional.of(expiredOtp));

        assertThrows(RuntimeException.class, () -> authService.resetPassword(request));
    }
}