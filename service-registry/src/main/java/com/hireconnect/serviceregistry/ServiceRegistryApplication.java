/**
 * Entry point for the Service Registry — Netflix Eureka server for microservice discovery.
 * All HireConnect services register with this Eureka server on startup.
 * Enables load-balanced Feign calls and dynamic routing via API Gateway.
 *
 * @author Disha Gujar
 */
package com.hireconnect.serviceregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
/**
 * Main entry point for the ServiceRegistryApplication.
 *
 * @author Disha Gujar
 */

@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryApplication{
    /**
     * Main.
     *
     * @author Disha Gujar
     */

	public static void main(String[] args) {
		SpringApplication.run(ServiceRegistryApplication.class, args);
	}
}
