// [Disha Gujar] : Entry point for the Auth Service — bootstraps Spring Boot with JPA and Feign Clients.
// [Disha Gujar] : Handles registration, login, JWT issuance, and Google OAuth2 flow.
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