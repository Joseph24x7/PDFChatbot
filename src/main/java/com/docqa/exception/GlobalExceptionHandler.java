package com.docqa.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        log.error("ValidationException: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(BAD_REQUEST.value(), "Validation failed", ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("ResourceNotFoundException: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(NOT_FOUND.value(), "Resource not found", ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("IllegalArgumentException: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(BAD_REQUEST.value(), "Invalid argument", ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        String details = ex.getBindingResult().getFieldErrors().stream().map(error -> error.getField() + ": " + error.getDefaultMessage()).reduce((a, b) -> a + ", " + b).orElse("Validation failed");
        ErrorResponse errorResponse = new ErrorResponse(BAD_REQUEST.value(), "Validation failed", details, LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("RuntimeException: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(INTERNAL_SERVER_ERROR.value(), "Internal server error", ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(INTERNAL_SERVER_ERROR.value(), "Unexpected error", ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, INTERNAL_SERVER_ERROR);
    }

}

