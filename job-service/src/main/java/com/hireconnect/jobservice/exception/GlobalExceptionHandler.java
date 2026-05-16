package com.hireconnect.jobservice.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
/**
 * Domain entity or core component representing GlobalExceptionHandler.
 *
 * @author Disha Gujar
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * Handle job not found.
     *
     * @author Disha Gujar
     */

    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleJobNotFound(JobNotFoundException ex, HttpServletRequest request) {
        log.warn("Job not found exception at path={}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }
    /**
     * Handle unauthorized.
     *
     * @author Disha Gujar
     */

    @ExceptionHandler(UnauthorizedJobAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedJobAccessException ex, HttpServletRequest request) {
        log.warn("Unauthorized access at path={}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
    }
    /**
     * Handle validation.
     *
     * @author Disha Gujar
     */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failed at path={}", request.getRequestURI());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("path", request.getRequestURI());

        Map<String, String> validations = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            validations.put(error.getField(), error.getDefaultMessage());
        }
        body.put("messages", validations);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    /**
     * Handle generic.
     *
     * @author Disha Gujar
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at path={}", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);
        return new ResponseEntity<>(body, status);
    }
}
