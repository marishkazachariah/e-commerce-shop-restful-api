package com.startstepszalando.ecommerceshop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.WebRequest;

import java.util.*;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        String errors = ex.getBindingResult().getFieldErrors()
                .stream().map(FieldError::getDefaultMessage).collect(Collectors.joining(", "));
        logger.error("Invalid input: " + errors);
        ErrorMessage message = new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                new Date(),
                "Invalid input: " + errors,
                request.getDescription(false));
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        logger.error("User Not Found Error: {}", ex.getMessage());
        ErrorMessage message = new ErrorMessage(
                HttpStatus.NOT_FOUND.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ErrorMessage> handleDuplicateUser(DuplicateUserException ex, WebRequest request) {
        String responseMessage = ex.getMessage() != null ? ex.getMessage() : "A user with this email already exists";
        logger.error("Duplicate User Error: {}", responseMessage);

        ErrorMessage message = new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                new Date(),
                responseMessage,
                request.getDescription(false));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<ErrorMessage> handleTokenValidationException(TokenValidationException ex, WebRequest request) {
        logger.error("Token Validation Error: {}", ex.getMessage());
        ErrorMessage message = new ErrorMessage(
                HttpStatus.UNAUTHORIZED.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(message, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorMessage> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        logger.error("Unauthorized Exception: {}", ex.getMessage());
        ErrorMessage message = new ErrorMessage(
                HttpStatus.UNAUTHORIZED.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<ErrorMessage>(message, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ErrorMessage> handleTokenRefreshException(TokenRefreshException ex, WebRequest request) {
        logger.error("Token Refresh Error: {}", ex.getMessage());
        ErrorMessage message = new ErrorMessage(
                HttpStatus.FORBIDDEN.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(message, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> globalExceptionHandler(Exception ex, WebRequest request) {
        logger.error("Internal Server Error: {}", ex.getMessage());
        ErrorMessage message = new ErrorMessage(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<ErrorMessage>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
