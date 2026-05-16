package com.hireconnect.profileservice.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleProfileNotFound_Success() {
        ProfileNotFoundException ex = new ProfileNotFoundException("Profile not found");
        ResponseEntity<String> response = handler.handleProfileNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Profile not found", response.getBody());
    }

    @Test
    void handleProfileAlreadyExists_Success() {
        ProfileAlreadyExistsException ex = new ProfileAlreadyExistsException("Profile exists");
        ResponseEntity<String> response = handler.handleProfileAlreadyExists(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Profile exists", response.getBody());
    }

    @Test
    void handleGenericException_Success() {
        Exception ex = new Exception("Generic error");
        ResponseEntity<String> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Generic error", response.getBody());
    }
}
