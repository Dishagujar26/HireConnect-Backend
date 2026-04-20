// [Disha Gujar] : Entry point for the API Gateway — the single entry point for all client requests.
// Validates JWT tokens, enforces role-based access control, and routes traffic to downstream
// microservices (auth, job, application, interview, payment, profile, notification) via Spring Cloud Gateway.
package com.hireconnect.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class ApiGatewayApplication {

    public static void main(String[] args) {
        log.info("Starting HireConnect API Gateway application");
        SpringApplication.run(ApiGatewayApplication.class, args);
        log.info("HireConnect API Gateway application started successfully");
    }
}
