package com.example.timetable.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ===============================
    // Validation Errors
    // ===============================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex
    ) {

        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError field : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(field.getField(), field.getDefaultMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // ===============================
    // User Not Found
    // ===============================
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request
    ) {

        return buildError(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // ===============================
    // Duplicate User
    // ===============================
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUserExists(
            UserAlreadyExistsException ex,
            HttpServletRequest request
    ) {

        return buildError(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // ===============================
    // Invalid Credentials
    // ===============================
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidLogin(
            InvalidCredentialsException ex,
            HttpServletRequest request
    ) {

        return buildError(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // ===============================
    // Entity Not Found (JPA)
    // ===============================
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleEntityNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request
    ) {

        return buildError(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // ===============================
    // Illegal Argument
    // ===============================
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegal(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // ===============================
    // Access Denied
    // ===============================
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {

        return buildError(
                HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action",
                request.getRequestURI()
        );
    }

    // ===============================
    // No Such Element
    // ===============================
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleNoSuchElement(
            NoSuchElementException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // ===============================
    // Data Integrity (unique constraint, etc.)
    // ===============================
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        String message = ex.getRootCause() != null
                ? ex.getRootCause().getMessage()
                : ex.getMessage();

        return buildError(
                HttpStatus.CONFLICT,
                message,
                request.getRequestURI()
        );
    }

    // ===============================
    // Illegal State
    // ===============================
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // ===============================
    // Fallback
    // ===============================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(
            Exception ex,
            HttpServletRequest request
    ) {

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                request.getRequestURI()
        );
    }

    // ===============================
    // Build Standard Error
    // ===============================
    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            String message,
            String path
    ) {

        ApiError error = new ApiError(
                status.value(),
                message,
                path
        );

        return new ResponseEntity<>(error, status);
    }
}