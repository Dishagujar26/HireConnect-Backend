package com.hireconnect.notificationservice.exception;
/**
 * Custom exception for handling BadRequestException scenarios.
 *
 * @author Disha Gujar
 */

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
