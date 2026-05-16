package com.hireconnect.profileservice.exception;
/**
 * Custom exception for handling ProfileAlreadyExistsException scenarios.
 *
 * @author Disha Gujar
 */

public class ProfileAlreadyExistsException extends RuntimeException {

    public ProfileAlreadyExistsException(String message) {
        super(message);
    }
}
