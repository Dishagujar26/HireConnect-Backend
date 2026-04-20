// [Disha Gujar] : Entry point for the Application Service — enables Feign Clients for inter-service communication.
// [Disha Gujar] : Responsible for candidate job applications, status tracking, and Kafka event publishing.
package com.hireconnect.applicationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ApplicationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApplicationServiceApplication.class, args);
	}

}
