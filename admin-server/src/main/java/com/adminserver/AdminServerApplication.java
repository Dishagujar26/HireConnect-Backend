/**
 * Entry point for the Admin Server — Spring Boot Admin dashboard for production-grade observability.
 * Aggregates health status, JVM metrics, and logging from all registered microservices.
 *
 * @author Disha Gujar
 */
package com.adminserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
/**
 * Main entry point for the AdminServerApplication.
 *
 * @author Disha Gujar
 */

@SpringBootApplication
@EnableAdminServer
public class AdminServerApplication {
    /**
     * Main.
     *
     * @author Disha Gujar
     */

	public static void main(String[] args) {
		SpringApplication.run(AdminServerApplication.class, args);
	}

}
