// [Disha Gujar] : Entry point for the Service Registry — Netflix Eureka server for microservice discovery.
// [Disha Gujar] : All HireConnect services register with this Eureka server on startup.
// [Disha Gujar] : Enables load-balanced Feign calls and dynamic routing via API Gateway.
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