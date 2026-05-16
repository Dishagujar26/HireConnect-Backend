package com.hireconnect.apigateway.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthHeaderRequestWrapperTest {

    @Mock
    private HttpServletRequest request;

    private AuthHeaderRequestWrapper wrapper;

    @BeforeEach
    void setUp() {
        wrapper = new AuthHeaderRequestWrapper(request, 1L, "test@test.com", "CANDIDATE");
    }

    @Test
    void getHeader_CustomHeader_ShouldReturnCorrectValue() {
        assertEquals("1", wrapper.getHeader("X-Auth-User-Id"));
        assertEquals("test@test.com", wrapper.getHeader("X-Auth-User-Email"));
        assertEquals("CANDIDATE", wrapper.getHeader("X-Auth-User-Role"));
    }

    @Test
    void getHeader_OriginalHeader_ShouldReturnCorrectValue() {
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        assertEquals("Mozilla/5.0", wrapper.getHeader("User-Agent"));
    }

    @Test
    void getHeaders_CustomHeader_ShouldReturnEnumeration() {
        Enumeration<String> headers = wrapper.getHeaders("X-Auth-User-Id");
        assertTrue(headers.hasMoreElements());
        assertEquals("1", headers.nextElement());
    }

    @Test
    void getHeaderNames_ShouldIncludeAll() {
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(List.of("User-Agent")));
        
        Enumeration<String> names = wrapper.getHeaderNames();
        int count = 0;
        boolean foundUserId = false;
        boolean foundUserAgent = false;
        
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            count++;
            if ("X-Auth-User-Id".equals(name)) foundUserId = true;
            if ("User-Agent".equals(name)) foundUserAgent = true;
        }
        
        assertTrue(foundUserId);
        assertTrue(foundUserAgent);
        assertEquals(4, count); // 3 custom + 1 original
    }
}
