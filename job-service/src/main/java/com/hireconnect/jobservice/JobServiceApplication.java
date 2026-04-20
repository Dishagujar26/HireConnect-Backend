// [Disha Gujar] : Entry point for the Job Service — responsible for the complete job posting lifecycle.
// Provides CRUD operations for jobs, advanced search/filter by keyword, location, job type, experience
// level and salary range, featured job marking, and internal ownership/existence checks for other services.
package com.hireconnect.jobservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JobServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobServiceApplication.class, args);
	}

}
