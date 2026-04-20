// [Disha Gujar] : Entry point for the Profile Service — manages user profile data for both candidates and recruiters.
// Handles profile creation, updates, candidate resume file uploads/downloads via cloud storage,
// and exposes internal Feign endpoints for cross-service profile data retrieval.
package com.hireconnect.profileservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ProfileServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProfileServiceApplication.class, args);
	}

}
