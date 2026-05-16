/**
 * Entry point for the Application Service.
 * This service manages candidate job applications, status tracking,
 * and integration with other services via Feign clients and Kafka.
 * @author Disha Gujar
 */
package com.hireconnect.applicationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
/**
 * Main entry point for the ApplicationServiceApplication.
 *
 * @author Disha Gujar
 */

@SpringBootApplication
@EnableFeignClients
public class ApplicationServiceApplication {
    /**
     * Main.
     *
     * @author Disha Gujar
     */

	public static void main(String[] args) {
		SpringApplication.run(ApplicationServiceApplication.class, args);
	}

}
