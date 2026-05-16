package com.hireconnect.apigateway.security;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class GatewayJwtAuthenticationFilterTest {

    private GatewayJwtAuthenticationFilter filter;

    @Mock
    private JwtService jwtService;

    @Mock
    private PublicEndpointService publicEndpointService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new GatewayJwtAuthenticationFilter(jwtService, publicEndpointService);
    }

    @Test
    void shouldNotFilter_PublicEndpoint_ShouldReturnTrue() {
        when(publicEndpointService.isPublic(request)).thenReturn(true);
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void doFilterInternal_OptionsRequest_ShouldPassThrough() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("OPTIONS");
        
        filter.doFilterInternal(request, response, filterChain);
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_NoAuthHeader_ShouldReturnUnauthorized() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(null);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void doFilterInternal_ValidToken_ShouldAuthenticateAndWrap() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractUserId("valid-token")).thenReturn(1L);
        when(jwtService.extractEmail("valid-token")).thenReturn("test@test.com");
        when(jwtService.extractRole("valid-token")).thenReturn("CANDIDATE");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(AuthHeaderRequestWrapper.class), eq(response));
    }

    private void assertTrue(boolean condition) {
        if (!condition) throw new AssertionError();
    }
}
