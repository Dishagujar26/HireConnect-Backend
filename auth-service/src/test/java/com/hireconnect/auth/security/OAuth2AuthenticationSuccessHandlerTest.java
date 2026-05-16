package com.hireconnect.auth.security;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.RedirectStrategy;

import com.hireconnect.auth.entity.RefreshToken;
import com.hireconnect.auth.entity.Role;
import com.hireconnect.auth.entity.UserCredential;
import com.hireconnect.auth.repository.AuthRepository;
import com.hireconnect.auth.service.RefreshTokenService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class OAuth2AuthenticationSuccessHandlerTest {

    private OAuth2AuthenticationSuccessHandler handler;
    private AuthRepository authRepository;
    private JwtService jwtService;
    private RefreshTokenService refreshTokenService;
    private PasswordEncoder passwordEncoder;
    private RedirectStrategy redirectStrategy;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private Authentication authentication;
    private OAuth2User oAuth2User;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        authRepository = mock(AuthRepository.class);
        jwtService = mock(JwtService.class);
        refreshTokenService = mock(RefreshTokenService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        redirectStrategy = mock(RedirectStrategy.class);

        handler = new OAuth2AuthenticationSuccessHandler(
                authRepository, jwtService, refreshTokenService, passwordEncoder, "http://localhost:4200/auth-success"
        );
        handler.setRedirectStrategy(redirectStrategy);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        authentication = mock(Authentication.class);
        oAuth2User = mock(OAuth2User.class);
        session = mock(HttpSession.class);

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(request.getSession()).thenReturn(session);
    }

    // --- Branch 1: Existing user ---
    @Test
    void onAuthenticationSuccess_ExistingUser_ShouldRedirectWithTokens() throws IOException, ServletException {
        when(oAuth2User.getAttribute("email")).thenReturn("test@gmail.com");
        UserCredential user = UserCredential.builder()
                .userId(1L).email("test@gmail.com").role(Role.CANDIDATE).build();

        when(authRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("access-token");
        when(refreshTokenService.createOrUpdateRefreshToken(user))
                .thenReturn(RefreshToken.builder().id(1L).token("refresh-token").user(user).build());

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains("accessToken=access-token"));
    }

    // --- Branch 2: New user with RECRUITER role ---
    @Test
    void onAuthenticationSuccess_NewUser_RECRUITER_ShouldCreateAndRedirect() throws IOException, ServletException {
        when(oAuth2User.getAttribute("email")).thenReturn("new@gmail.com");
        when(session.getAttribute("OAUTH_SELECTED_ROLE")).thenReturn("RECRUITER");
        when(authRepository.findByEmail("new@gmail.com")).thenReturn(Optional.empty());

        UserCredential savedUser = UserCredential.builder()
                .userId(2L).email("new@gmail.com").role(Role.RECRUITER).build();

        when(authRepository.save(any())).thenReturn(savedUser);
        when(jwtService.generateToken(any())).thenReturn("access-token");
        when(refreshTokenService.createOrUpdateRefreshToken(any()))
                .thenReturn(RefreshToken.builder().id(2L).token("refresh-token").user(savedUser).build());

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(authRepository).save(any());
        verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains("role=RECRUITER"));
    }

    // --- Branch 3: New user with CANDIDATE role ---
    @Test
    void onAuthenticationSuccess_NewUser_CANDIDATE_ShouldCreateAndRedirect() throws IOException, ServletException {
        when(oAuth2User.getAttribute("email")).thenReturn("candidate@gmail.com");
        when(session.getAttribute("OAUTH_SELECTED_ROLE")).thenReturn("CANDIDATE");
        when(authRepository.findByEmail("candidate@gmail.com")).thenReturn(Optional.empty());

        UserCredential savedUser = UserCredential.builder()
                .userId(3L).email("candidate@gmail.com").role(Role.CANDIDATE).build();

        when(authRepository.save(any())).thenReturn(savedUser);
        when(jwtService.generateToken(any())).thenReturn("access-token");
        when(refreshTokenService.createOrUpdateRefreshToken(any()))
                .thenReturn(RefreshToken.builder().id(3L).token("refresh-token").user(savedUser).build());

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains("role=CANDIDATE"));
    }

    // --- Branch 4: null email -> redirect to error ---
    @Test
    void onAuthenticationSuccess_NullEmail_ShouldRedirectWithError() throws IOException, ServletException {
        when(oAuth2User.getAttribute("email")).thenReturn(null);

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect(contains("error=Email+not+found"));
    }

    // --- Branch 5: blank email -> redirect to error ---
    @Test
    void onAuthenticationSuccess_BlankEmail_ShouldRedirectWithError() throws IOException, ServletException {
        when(oAuth2User.getAttribute("email")).thenReturn("  ");

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect(contains("error=Email+not+found"));
    }

    // --- Branch 6: New user, no role in session -> redirect to error ---
    @Test
    void onAuthenticationSuccess_NewUser_NoRoleInSession_ShouldRedirectWithError() throws IOException, ServletException {
        when(oAuth2User.getAttribute("email")).thenReturn("norole@gmail.com");
        when(session.getAttribute("OAUTH_SELECTED_ROLE")).thenReturn(null);
        when(authRepository.findByEmail("norole@gmail.com")).thenReturn(Optional.empty());

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect(contains("error=Role+selection+missing+or+invalid"));
    }

    // --- Branch 7: New user, invalid role string in session -> redirect to error ---
    @Test
    void onAuthenticationSuccess_NewUser_InvalidRole_ShouldRedirectWithError() throws IOException, ServletException {
        when(oAuth2User.getAttribute("email")).thenReturn("badrole@gmail.com");
        when(session.getAttribute("OAUTH_SELECTED_ROLE")).thenReturn("INVALID_ROLE");
        when(authRepository.findByEmail("badrole@gmail.com")).thenReturn(Optional.empty());

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect(contains("error=Role+selection+missing+or+invalid"));
    }

    // --- Branch 8: New user, ADMIN role submitted (blocked) -> redirect to error ---
    @Test
    void onAuthenticationSuccess_NewUser_AdminRole_ShouldRedirectWithError() throws IOException, ServletException {
        when(oAuth2User.getAttribute("email")).thenReturn("admin@gmail.com");
        when(session.getAttribute("OAUTH_SELECTED_ROLE")).thenReturn("ADMIN");
        when(authRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.empty());

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect(contains("error=Role+selection+missing+or+invalid"));
    }

    // --- Branch 9: Existing user, verify session attribute is removed ---
    @Test
    void onAuthenticationSuccess_ExistingUser_ShouldRemoveSessionAttribute() throws IOException, ServletException {
        when(oAuth2User.getAttribute("email")).thenReturn("existing@gmail.com");
        UserCredential user = UserCredential.builder()
                .userId(4L).email("existing@gmail.com").role(Role.CANDIDATE).build();

        when(authRepository.findByEmail("existing@gmail.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("token");
        when(refreshTokenService.createOrUpdateRefreshToken(user))
                .thenReturn(RefreshToken.builder().id(4L).token("refresh").user(user).build());

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(session).removeAttribute("OAUTH_SELECTED_ROLE");
    }
}
