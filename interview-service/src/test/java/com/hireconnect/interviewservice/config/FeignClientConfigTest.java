package com.hireconnect.interviewservice.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;

class FeignClientConfigTest {

    @Test
    void requestInterceptor_WithRequest_ShouldAddHeaders() {
        FeignClientConfig config = new FeignClientConfig();
        RequestInterceptor interceptor = config.requestInterceptor();
        
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Auth-User-Id")).thenReturn("1");
        
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
        
        try {
            RequestTemplate template = new RequestTemplate();
            interceptor.apply(template);
            assertTrue(template.headers().containsKey("X-Auth-User-Id"));
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void requestInterceptor_NoRequest_ShouldNotAddHeaders() {
        FeignClientConfig config = new FeignClientConfig();
        RequestInterceptor interceptor = config.requestInterceptor();
        
        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);
        
        assertFalse(template.headers().containsKey("X-Auth-User-Id"));
    }
}
