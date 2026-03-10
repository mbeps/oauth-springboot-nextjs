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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller advice that centralizes exception handling across all controllers.
 *
 * <p>
 * Transforms common Spring exceptions into consistent JSON error responses.
 * Handlers cover validation errors, authentication/authorization failures,
 * illegal arguments, and any uncaught exceptions. Each response includes a
 * timestamp, HTTP status code, an error string, and a message. The helper
 * {@link #buildErrorMap} constructs the response body.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    /**
     * Handles {@link MethodArgumentNotValidException} thrown when request body
     * validation fails. Builds a map of field errors and returns HTTP 400
     * along with details.
     *
     * @param ex exception containing validation results
     * @return 400 response with error map
     */
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

    @ExceptionHandler(AuthenticationException.class)
    /**
     * Catches any Spring Security {@link AuthenticationException}, typically
     * thrown when credentials are missing or invalid. Returns a 401 JSON error.
     *
     * @param ex authentication exception
     * @return 401 response with error details
     */
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildErrorMap(HttpStatus.UNAUTHORIZED, "Authentication Failed", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    /**
     * Invoked when a user is authenticated but lacks required authorities (403).
     *
     * @param ex access denied exception
     * @return 403 response with a generic permission error message
     */
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildErrorMap(HttpStatus.FORBIDDEN, "Access Denied",
                        "You don't have permission to access this resource"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    /**
     * Converts an {@link IllegalArgumentException} into a 400 Bad Request
     * response. Useful for programmatic validation failures thrown inside
     * controllers or services.
     *
     * @param ex illegal argument exception
     * @return 400 response with error message
     */
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(buildErrorMap(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    /**
     * Fallback handler for all other {@link Exception} types. Logs the error and
     * returns a 500 Internal Server Error JSON payload to avoid leaking stack
     * traces.
     *
     * @param ex unexpected exception
     * @return 500 response with generic error message
     */
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorMap(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                        "An unexpected error occurred"));
    }

    /**
     * Common helper that constructs the response body used by several handlers.
     *
     * @param status  HTTP status to report
     * @param error   short error string
     * @param message descriptive message
     * @return map suitable for serialization as JSON
     */
    private Map<String, Object> buildErrorMap(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return body;
    }
}
