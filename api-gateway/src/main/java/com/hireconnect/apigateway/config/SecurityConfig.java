package com.hireconnect.apigateway.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.hireconnect.apigateway.security.GatewayJwtAuthenticationFilter;
import com.hireconnect.apigateway.security.JwtService;
import com.hireconnect.apigateway.security.PublicEndpointService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// [Disha Gujar] : Security configuration for the API Gateway — defines the stateless JWT filter chain.
// Permits public endpoints (auth, webhook, actuator, swagger), applies CORS policy for the Angular frontend
// at localhost:4200, and installs the GatewayJwtAuthenticationFilter before Spring's default auth filter.
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtService jwtService;
    private final PublicEndpointService publicEndpointService;

    @Bean
    public GatewayJwtAuthenticationFilter gatewayJwtAuthenticationFilter() {
        log.info("Creating GatewayJwtAuthenticationFilter bean");
        return new GatewayJwtAuthenticationFilter(jwtService, publicEndpointService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring API Gateway security filter chain");

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/payments/webhook",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/aggregate/**",
                                "/actuator/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(gatewayJwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        log.info("API Gateway security filter chain configured successfully");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Configuring CORS for API Gateway");

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("CORS configured with allowed origin: http://localhost:4200");
        return source;
    }
}
