// [Disha Gujar] : Entry point for the Auth Service — bootstraps Spring Boot application with JPA repositories,
// Feign Clients, and entity scanning. Handles user registration, login, JWT issuance, refresh tokens,
// forgot/reset password via OTP, and Google OAuth2 social login flow.
package com.hireconnect.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients
@EnableJpaRepositories(basePackages = "com.hireconnect.auth.repository")
@EntityScan(basePackages = "com.hireconnect.auth.entity")
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}