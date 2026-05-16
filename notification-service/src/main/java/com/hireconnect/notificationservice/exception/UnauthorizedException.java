package com.hireconnect.notificationservice.exception;
/**
 * Custom exception for handling UnauthorizedException scenarios.
 *
 * @author Disha Gujar
 */

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
