package com.hireconnect.auth.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hireconnect.auth.dto.request.ForgotPasswordRequest;
import com.hireconnect.auth.dto.request.LoginRequest;
import com.hireconnect.auth.dto.request.RefreshTokenRequest;
import com.hireconnect.auth.dto.request.RegisterRequest;
import com.hireconnect.auth.dto.request.ResetPasswordRequest;
import com.hireconnect.auth.dto.response.AuthResponse;
import com.hireconnect.auth.dto.response.TokenValidationResponse;
import com.hireconnect.auth.entity.Role;
import com.hireconnect.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

// [Disha Gujar] : REST controller exposing authentication endpoints under /api/v1/auth.
// Supports user registration, email/password login, JWT refresh, forgot/reset password via OTP,
// token validation for the API Gateway, and Google OAuth2 role-selection redirect flow.
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final String OAUTH_SELECTED_ROLE = "OAUTH_SELECTED_ROLE";

    private final AuthService authService;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Validated @RequestBody RegisterRequest request) {
        log.info("Register request received for email: {}, role: {}", request.getEmail(), request.getRole());
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Validated @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Validated @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token request received");
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Validated @RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password request received for email: {}", request.getEmail());
        authService.forgotPassword(request);
        return ResponseEntity.ok("OTP sent successfully to your email");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Validated @RequestBody ResetPasswordRequest request) {
        log.info("Reset password request received for email: {}", request.getEmail());
        authService.resetPassword(request);
        return ResponseEntity.ok("Password reset successfully");
    }

    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        log.info("Token validation request received");
        return ResponseEntity.ok(authService.validateToken(authHeader));
    }

    @GetMapping("/oauth2/authorize/google")
    public void authorizeGoogle(
            @RequestParam("role") String roleParam,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        log.info("Google OAuth authorization requested with role: {}", roleParam);

        Role selectedRole;
        try {
            selectedRole = Role.valueOf(roleParam.toUpperCase());
        } catch (Exception ex) {
            log.warn("Invalid role received for Google OAuth authorization: {}", roleParam);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid role");
            return;
        }

        if (selectedRole != Role.CANDIDATE && selectedRole != Role.RECRUITER) {
            log.warn("Unsupported role received for Google OAuth authorization: {}", selectedRole);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Only CANDIDATE or RECRUITER allowed");
            return;
        }

        request.getSession(true).setAttribute(OAUTH_SELECTED_ROLE, selectedRole.name());
        log.info("Redirecting to Google OAuth flow for role: {}", selectedRole);
        redirectStrategy.sendRedirect(request, response, "/oauth2/authorization/google");
    }
}
