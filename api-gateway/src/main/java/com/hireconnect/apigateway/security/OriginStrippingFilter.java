package com.hireconnect.apigateway.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;

/**
 * Filter that runs after Spring Security's CORS validation.
 * It strips the "Origin" header from requests forwarded to downstream microservices,
 * ensuring they process the requests as internal/direct calls and don't trigger CORS 403 blocks.
 *
 * @author Antigravity
 */
@Component
@Slf4j
public class OriginStrippingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String path = httpRequest.getRequestURI();
            
            // Wrap request to hide Origin header from downstream gateway router
            HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(httpRequest) {
                @Override
                public String getHeader(String name) {
                    if ("origin".equalsIgnoreCase(name)) {
                        return null;
                    }
                    return super.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if ("origin".equalsIgnoreCase(name)) {
                        return Collections.emptyEnumeration();
                    }
                    return super.getHeaders(name);
                }

                @Override
                public Enumeration<String> getHeaderNames() {
                    List<String> names = new ArrayList<>(Collections.list(super.getHeaderNames()));
                    names.removeIf(name -> "origin".equalsIgnoreCase(name));
                    return Collections.enumeration(names);
                }
            };
            
            log.debug("OriginStrippingFilter: Stripping Origin header for path: {}", path);
            chain.doFilter(wrappedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
