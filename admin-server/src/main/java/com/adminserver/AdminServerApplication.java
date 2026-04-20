// [Disha Gujar] : Entry point for the Admin Server — Spring Boot Admin dashboard for production-grade observability.
// Aggregates health status, JVM metrics, log levels, environment properties, and HTTP traces
// from all registered HireConnect microservices in a single management UI.
package com.adminserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

@SpringBootApplication
@EnableAdminServer
public class AdminServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdminServerApplication.class, args);
	}

}
