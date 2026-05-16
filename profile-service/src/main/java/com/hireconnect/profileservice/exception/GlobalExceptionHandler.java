package com.hireconnect.profileservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
/**
 * Domain entity or core component representing GlobalExceptionHandler.
 *
 * @author Disha Gujar
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * Handle profile not found.
     *
     * @author Disha Gujar
     */

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<String> handleProfileNotFound(ProfileNotFoundException ex) {
        log.warn("Profile not found: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
    /**
     * Handle profile already exists.
     *
     * @author Disha Gujar
     */

    @ExceptionHandler(ProfileAlreadyExistsException.class)
    public ResponseEntity<String> handleProfileAlreadyExists(ProfileAlreadyExistsException ex) {
        log.warn("Profile already exists: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }
    /**
     * Handle generic exception.
     *
     * @author Disha Gujar
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        log.error("Unhandled exception occurred in profile service", ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
