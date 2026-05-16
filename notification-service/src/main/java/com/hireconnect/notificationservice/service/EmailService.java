package com.hireconnect.notificationservice.service;
/**
 * Service interface defining the contract for Email business logic.
 *
 * @author Disha Gujar
 */

public interface EmailService {

    void sendEmail(String to, String subject, String body);
}
