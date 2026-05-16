/**
 * Entry point for the Job Service.
 * This service handles the complete job posting lifecycle, including CRUD operations,
 * advanced search/filtering, and recruiter ownership checks.
 * @author Disha Gujar
 */
package com.hireconnect.jobservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * Main entry point for the JobServiceApplication.
 *
 * @author Disha Gujar
 */

@SpringBootApplication
public class JobServiceApplication {
    /**
     * Main.
     *
     * @author Disha Gujar
     */

	public static void main(String[] args) {
		SpringApplication.run(JobServiceApplication.class, args);
	}

}
