package com.hireconnect.applicationservice.exception;
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
