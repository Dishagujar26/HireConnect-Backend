package com.hireconnect.profileservice.exception;
/**
 * Custom exception for handling ProfileNotFoundException scenarios.
 *
 * @author Disha Gujar
 */

public class ProfileNotFoundException extends RuntimeException {

    public ProfileNotFoundException(String message) {
        super(message);
    }
}
