/**
 * Entry point for the Notification Service.
 * This service handles event-driven email notifications by consuming Kafka topics
 * and dispatching transactional emails via JavaMailSender.
 * @author Disha Gujar
 */
package com.hireconnect.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * Main entry point for the NotificationServiceApplication.
 *
 * @author Disha Gujar
 */

@SpringBootApplication
public class NotificationServiceApplication {
    /**
     * Main.
     *
     * @author Disha Gujar
     */

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

}
