// [Disha Gujar] : Entry point for the Interview Service — enables Feign Clients for cross-service data access.
// [Disha Gujar] : Manages the full interview lifecycle: scheduling, updates, and cancellations.
package com.hireconnect.interviewservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class InterviewServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InterviewServiceApplication.class, args);
	}

}
