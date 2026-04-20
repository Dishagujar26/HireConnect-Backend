// [Disha Gujar] : Entry point for the Notification Service — event-driven email notification system.
// Consumes Kafka topics published by other microservices (auth, application, interview) and dispatches
// transactional emails via JavaMailSender for registration, application status changes, and interview alerts.
package com.hireconnect.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

}
