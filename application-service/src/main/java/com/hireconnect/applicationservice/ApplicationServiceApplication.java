// [Disha Gujar] : Entry point for the Application Service — enables Feign Clients for inter-service communication.
// Responsible for processing candidate job applications, tracking application lifecycle statuses
// (APPLIED, SHORTLISTED, REJECTED, HIRED), and publishing Kafka events to trigger notifications.
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
