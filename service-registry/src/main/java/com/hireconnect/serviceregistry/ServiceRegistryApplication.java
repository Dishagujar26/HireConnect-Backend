// [Disha Gujar] : Entry point for the Service Registry — Netflix Eureka server for microservice discovery.
// All HireConnect services register with this Eureka server on startup, enabling load-balanced
// Feign client calls and dynamic routing through the API Gateway without hard-coded service URLs.
package com.hireconnect.serviceregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceRegistryApplication.class, args);
	}
}