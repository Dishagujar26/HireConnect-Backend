// [Disha Gujar] : Entry point for the Interview Service — enables Feign Clients for cross-service data access.
// Manages the full interview lifecycle: scheduling interviews by recruiters, updating interview details,
// cancellation workflows, and providing interview views for both recruiter and candidate roles.
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
