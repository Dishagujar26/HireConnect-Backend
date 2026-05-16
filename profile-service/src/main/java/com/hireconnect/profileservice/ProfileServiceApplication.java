/**
 * Entry point for the Profile Service.
 * This service manages user profile data for both candidates and recruiters,
 * including profile details, resume uploads, and cross-service profile retrieval.
 * @author Disha Gujar
 */
package com.hireconnect.profileservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
/**
 * Main entry point for the ProfileServiceApplication.
 *
 * @author Disha Gujar
 */

@SpringBootApplication
@EnableFeignClients
public class ProfileServiceApplication {
    /**
     * Main.
     *
     * @author Disha Gujar
     */

	public static void main(String[] args) {
		SpringApplication.run(ProfileServiceApplication.class, args);
	}

}
