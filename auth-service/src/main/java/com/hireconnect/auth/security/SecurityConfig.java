package com.hireconnect.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.hireconnect.auth.repository.AuthRepository;
import com.hireconnect.auth.service.RefreshTokenService;

/**
 * Security configuration for the Auth Service.
 * Configures password encoding, OAuth2 login handlers, and the security filter chain.
 * @author Disha Gujar
 */
@Configuration
public class SecurityConfig {

    @Value("${auth.oauth-success-redirect-url}")
    private String successRedirectUrl;

    @Value("${auth.oauth-failure-redirect-url}")
    private String failureRedirectUrl;

    /**
     * Configures the OAuth2 authentication success handler.
     * 
     * @param authRepository the auth repository
     * @param jwtService the JWT service
     * @param refreshTokenService the refresh token service
     * @param passwordEncoder the password encoder
     * @return a configured OAuth2AuthenticationSuccessHandler
     
 * @author Disha Gujar
 */
    @Bean
    public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler(
            AuthRepository authRepository,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            PasswordEncoder passwordEncoder
    ) {
        return new OAuth2AuthenticationSuccessHandler(
                authRepository,
                jwtService,
                refreshTokenService,
                passwordEncoder,
                successRedirectUrl
        );
    }
    /**
     * O auth2 authentication failure handler.
     *
     * @author Disha Gujar
     */

    @Bean
    public OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
        return new OAuth2AuthenticationFailureHandler(failureRedirectUrl);
    }

    /**
     * Configures the HTTP security filter chain.
     * Disables CSRF, allows public access to auth endpoints, and sets up OAuth2 login.
     * 
     * @param http the HttpSecurity object
     * @param oAuth2AuthenticationSuccessHandler the success handler for OAuth2
     * @param oAuth2AuthenticationFailureHandler the failure handler for OAuth2
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     
 * @author Disha Gujar
 */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
            OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler
    ) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/api/v1/auth/**",
                        "/api/v1/admin/**",
                        "/oauth2/**",
                        "/login/oauth2/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/actuator/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth -> oauth
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)
            );

        return http.build();
    }



    /**
     * Configures the password encoder using BCrypt.
     * 
     * @return a BCryptPasswordEncoder instance
     
 * @author Disha Gujar
 */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
