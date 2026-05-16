package com.hireconnect.notificationservice.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_ShouldReturn404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().get("message"));
    }

    @Test
    void handleBadRequest_ShouldReturn400() {
        BadRequestException ex = new BadRequestException("Bad request");
        ResponseEntity<Map<String, Object>> response = handler.handleBadRequest(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad request", response.getBody().get("message"));
    }

    @Test
    void handleUnauthorized_ShouldReturn403() {
        UnauthorizedException ex = new UnauthorizedException("Unauthorized");
        ResponseEntity<Map<String, Object>> response = handler.handleUnauthorized(ex);
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Unauthorized", response.getBody().get("message"));
    }

    @Test
    void handleGeneric_ShouldReturn500() {
        Exception ex = new Exception("Error");
        ResponseEntity<Map<String, Object>> response = handler.handleGeneric(ex);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Something went wrong", response.getBody().get("message"));
    }
}
