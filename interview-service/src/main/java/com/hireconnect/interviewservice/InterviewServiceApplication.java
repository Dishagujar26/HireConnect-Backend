/**
 * Entry point for the Interview Service — enables Feign Clients for cross-service data access.
 * Manages the full interview lifecycle: scheduling, updates, and cancellations.
 *
 * @author Disha Gujar
 */
package com.hireconnect.interviewservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
/**
 * Main entry point for the InterviewServiceApplication.
 *
 * @author Disha Gujar
 */

@SpringBootApplication
@EnableFeignClients
public class InterviewServiceApplication {
    /**
     * Main.
     *
     * @author Disha Gujar
     */

	public static void main(String[] args) {
		SpringApplication.run(InterviewServiceApplication.class, args);
	}

}
