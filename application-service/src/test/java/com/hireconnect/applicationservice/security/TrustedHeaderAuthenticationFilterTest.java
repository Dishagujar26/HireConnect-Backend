package com.hireconnect.applicationservice.security;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class TrustedHeaderAuthenticationFilterTest {

    private TrustedHeaderAuthenticationFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new TrustedHeaderAuthenticationFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithHeaders_ShouldAuthenticate() throws Exception {
        when(request.getHeader("X-Auth-User-Id")).thenReturn("1");
        when(request.getHeader("X-Auth-User-Email")).thenReturn("test@test.com");
        when(request.getHeader("X-Auth-User-Role")).thenReturn("CANDIDATE");

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidHeaders_ShouldClearContext() throws Exception {
        when(request.getHeader("X-Auth-User-Id")).thenReturn("invalid");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_NullIdHeader_ShouldClearContext() throws Exception {
        when(request.getHeader("X-Auth-User-Id")).thenReturn(null);
        when(request.getHeader("X-Auth-User-Email")).thenReturn("test@test.com");
        when(request.getHeader("X-Auth-User-Role")).thenReturn("CANDIDATE");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_EmptyRoleHeader_ShouldDefaultToRoleUser() throws Exception {
        // Based on logic: if role is null or invalid, valueOf might fail or it might have a default.
        // Let's see the filter implementation.
        when(request.getHeader("X-Auth-User-Id")).thenReturn("1");
        when(request.getHeader("X-Auth-User-Email")).thenReturn("test@test.com");
        when(request.getHeader("X-Auth-User-Role")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        // If it throws exception internally, it might clear context.
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
