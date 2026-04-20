package com.hireconnect.auth.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.hireconnect.auth.entity.AuthProvider;
import com.hireconnect.auth.entity.RefreshToken;
import com.hireconnect.auth.entity.Role;
import com.hireconnect.auth.entity.UserCredential;
import com.hireconnect.auth.repository.AuthRepository;
import com.hireconnect.auth.service.RefreshTokenService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);
    private static final String OAUTH_SELECTED_ROLE = "OAUTH_SELECTED_ROLE";

    private final AuthRepository authRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final String successRedirectUrl;

    public OAuth2AuthenticationSuccessHandler(
            AuthRepository authRepository,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            PasswordEncoder passwordEncoder,
            String successRedirectUrl
    ) {
        this.authRepository = authRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
        this.successRedirectUrl = successRedirectUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        log.info("Google OAuth authentication success handler invoked");

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        if (email == null || email.isBlank()) {
            log.warn("Google OAuth failed because email was not found in Google account");
            response.sendRedirect(buildFailureUrl("Email not found from Google account"));
            return;
        }

        String selectedRoleValue = (String) request.getSession().getAttribute(OAUTH_SELECTED_ROLE);
        Role selectedRole = null;

        if (selectedRoleValue != null) {
            try {
                selectedRole = Role.valueOf(selectedRoleValue);
            } catch (Exception ignored) {
                log.warn("Invalid selected role found in session for email: {}", email);
            }
        }

        UserCredential existingUser = authRepository.findByEmail(email).orElse(null);

        UserCredential user;
        if (existingUser != null) {
            user = existingUser;
            log.info("Existing Google OAuth user found for userId: {}, email: {}, role: {}",
                    user.getUserId(), user.getEmail(), user.getRole());
        } else {
            if (selectedRole == null || (selectedRole != Role.CANDIDATE && selectedRole != Role.RECRUITER)) {
                log.warn("Google OAuth failed because role selection is missing or invalid for email: {}", email);
                response.sendRedirect(buildFailureUrl("Role selection missing or invalid"));
                return;
            }

            user = UserCredential.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(selectedRole)
                    .provider(AuthProvider.GOOGLE)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            user = authRepository.save(user);
            log.info("New Google OAuth user created with userId: {}, email: {}, role: {}",
                    user.getUserId(), user.getEmail(), user.getRole());
        }

        request.getSession().removeAttribute(OAUTH_SELECTED_ROLE);

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createOrUpdateRefreshToken(user);

        String redirectUrl = successRedirectUrl
                + "?accessToken=" + encode(accessToken)
                + "&refreshToken=" + encode(refreshToken.getToken())
                + "&email=" + encode(user.getEmail())
                + "&role=" + encode(user.getRole().name())
                + "&userId=" + user.getUserId();

        log.info("Redirecting Google OAuth user to success URL for userId: {}", user.getUserId());
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String buildFailureUrl(String message) {
        return successRedirectUrl + "?error=" + encode(message);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
