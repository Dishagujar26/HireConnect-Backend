/**
 * Entry point for the API Gateway — the single entry point for all client requests.
 * Validates JWT tokens, enforces RBAC, and routes traffic to downstream microservices.
 *
 * @author Disha Gujar
 */
package com.hireconnect.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;
/**
 * Main entry point for the ApiGatewayApplication.
 *
 * @author Disha Gujar
 */

@SpringBootApplication
@Slf4j
public class ApiGatewayApplication {
    /**
     * Main.
     *
     * @author Disha Gujar
     */

    public static void main(String[] args) {
        log.info("Starting HireConnect API Gateway application");
        SpringApplication.run(ApiGatewayApplication.class, args);
        log.info("HireConnect API Gateway application started successfully");
    }
}
