package com.hireconnect.applicationservice.exception;
/**
 * Custom exception for handling ResourceNotFoundException scenarios.
 *
 * @author Disha Gujar
 */

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
