package com.hireconnect.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

import lombok.extern.slf4j.Slf4j;

/**
 * Main entry point for the Config Server.
 * This service centralizes configuration management for all microservices in the HireConnect platform.
 * It fetches configuration files from the configured Git repository.
 *
 * @author Disha Gujar
 */
@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
@Slf4j
public class ConfigServerApplication {

    public static void main(String[] args) {
        log.info("Starting HireConnect Config Server...");
        SpringApplication.run(ConfigServerApplication.class, args);
        log.info("HireConnect Config Server started successfully on port 8888.");
    }
}
