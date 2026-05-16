package com.hireconnect.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;
/**
 * Configuration class for FeignClientConfig.
 *
 * @author Disha Gujar
 */

@Configuration
public class FeignClientConfig {
    /**
     * Request interceptor.
     *
     * @author Disha Gujar
     */

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
        };
    }
}
