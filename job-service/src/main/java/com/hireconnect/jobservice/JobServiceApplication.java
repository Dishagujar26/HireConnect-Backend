// [Disha Gujar] : Entry point for the Job Service — responsible for the complete job posting lifecycle.
// [Disha Gujar] : Provides CRUD operations, advanced search/filter, and recruiter ownership checks.
package com.hireconnect.jobservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JobServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobServiceApplication.class, args);
	}

}
