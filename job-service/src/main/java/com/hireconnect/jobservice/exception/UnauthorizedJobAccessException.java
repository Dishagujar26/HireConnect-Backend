package com.hireconnect.jobservice.exception;
/**
 * Custom exception for handling UnauthorizedJobAccessException scenarios.
 *
 * @author Disha Gujar
 */

public class UnauthorizedJobAccessException extends RuntimeException {
    public UnauthorizedJobAccessException(String message) {
        super(message);
    }
}
