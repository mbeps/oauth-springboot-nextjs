package com.maruf.oauth.exception;

import com.maruf.oauth.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides consistent JSON responses for common Spring MVC errors.
 * Keeps logging and payload formatting in one place to simplify troubleshooting.
 *
 * @author Maruf Bepary
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles bean validation errors and returns field level messages.
     * Uses maps so frontend forms can bind errors without extra parsing.
     *
     * @param ex raised validation exception containing binding results
     * @author Maruf Bepary
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("errors", errors);

        log.warn("Validation error: {}", errors);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Converts Spring Security authentication failures into HTTP 401 responses.
     * Includes the exception message to aid debugging login issues.
     *
     * @param ex authentication exception thrown by Spring Security
     * @author Maruf Bepary
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", "Authentication Failed");
        response.put("message", ex.getMessage());

        log.warn("Authentication error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Responds with HTTP 403 when a user lacks required permissions.
     * Avoids leaking backend details by returning a generic message.
     *
     * @param ex access denial raised by authorization checks
     * @author Maruf Bepary
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("error", "Access Denied");
        response.put("message", "You don't have permission to access this resource");

        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Maps {@link IllegalArgumentException} to a 400 Bad Request payload.
     * Keeps the thrown message for clarity while maintaining consistent structure.
     *
     * @param ex invalid argument encountered by service or controller layers
     * @author Maruf Bepary
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        response.put("message", ex.getMessage());

        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Catches unexpected errors and returns a generic 500 response.
     * Logs the exception stack trace to preserve debugging context.
     *
     * @param ex unhandled exception bubbled up to the controller advice
     * @author Maruf Bepary
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", "An unexpected error occurred");

        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Handles insufficient OAuth scope exceptions.
     * Returns 403 when required OAuth permissions were not granted.
     *
     * @param ex insufficient scope exception from OAuth validation
     * @author Maruf Bepary
     */
    @ExceptionHandler(InsufficientScopeException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientScopeException(InsufficientScopeException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .error("insufficient_scope")
                .message(ex.getMessage())
                .build();

        log.warn("Insufficient OAuth scope: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}