/**
 * Entry point for the Authentication Service.
 * This service handles user registration, login, JWT issuance, and Google OAuth2 flow.
 * It bootstraps Spring Boot with JPA and Feign Clients.
 * @author Disha Gujar
 */
package com.hireconnect.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
/**
 * Main entry point for the AuthServiceApplication.
 *
 * @author Disha Gujar
 */

@SpringBootApplication
@EnableFeignClients
@EnableJpaRepositories(basePackages = "com.hireconnect.auth.repository")
@EntityScan(basePackages = "com.hireconnect.auth.entity")
public class AuthServiceApplication {
    /**
     * Main.
     *
     * @author Disha Gujar
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
