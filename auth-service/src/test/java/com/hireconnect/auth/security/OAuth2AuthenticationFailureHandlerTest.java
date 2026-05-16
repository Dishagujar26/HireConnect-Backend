package com.hireconnect.auth.security;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.RedirectStrategy;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class OAuth2AuthenticationFailureHandlerTest {

    private OAuth2AuthenticationFailureHandler handler;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private AuthenticationException exception;
    private RedirectStrategy redirectStrategy;

    @BeforeEach
    void setUp() {
        handler = new OAuth2AuthenticationFailureHandler("http://localhost:4200/login");
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        exception = mock(AuthenticationException.class);
        redirectStrategy = mock(RedirectStrategy.class);
        handler.setRedirectStrategy(redirectStrategy);
    }

    @Test
    void onAuthenticationFailure_ShouldRedirectWithError() throws IOException, ServletException {
        when(exception.getMessage()).thenReturn("Invalid user");

        handler.onAuthenticationFailure(request, response, exception);

        verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains("error=Invalid+user"));
    }
    @Test
    void onAuthenticationFailure_NullMessage_ShouldRedirectWithDefaultError() throws IOException, ServletException {
        when(exception.getMessage()).thenReturn(null);

        handler.onAuthenticationFailure(request, response, exception);

        verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains("error=Google+login+failed"));
    }
}
