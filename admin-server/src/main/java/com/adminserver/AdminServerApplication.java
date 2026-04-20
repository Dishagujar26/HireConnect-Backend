// [Disha Gujar] : Entry point for the Admin Server — Spring Boot Admin dashboard for production-grade observability.
// [Disha Gujar] : Aggregates health status, JVM metrics, and logging from all registered microservices.
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
