// [Disha Gujar] : Entry point for the Notification Service — event-driven email notification system.
// [Disha Gujar] : Consumes Kafka topics and dispatches transactional emails via JavaMailSender.
package com.hireconnect.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

}
