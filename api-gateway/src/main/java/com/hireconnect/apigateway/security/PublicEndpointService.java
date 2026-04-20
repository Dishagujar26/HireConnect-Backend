package com.hireconnect.apigateway.security;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PublicEndpointService {

    public boolean isPublic(HttpServletRequest request) {
        String path = request.getRequestURI();

        boolean isPublic = path.startsWith("/api/v1/auth/")
                || path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/")
                || path.startsWith("/swagger-ui/")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/aggregate/")
                || path.startsWith("/api/payments/webhook")
                || path.startsWith("/actuator/");

        log.debug("Endpoint visibility check for path {}: {}", path, isPublic ? "PUBLIC" : "PROTECTED");
        return isPublic;
    }
}