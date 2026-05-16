package com.hireconnect.jobservice.exception;
/**
 * Custom exception for handling JobNotFoundException scenarios.
 *
 * @author Disha Gujar
 */

public class JobNotFoundException extends RuntimeException {
    public JobNotFoundException(String message) {
        super(message);
    }
}
