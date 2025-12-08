package com.maruf.oauth.exception;

/**
 * Thrown when required OAuth scopes were not granted by the user.
 * Allows authentication to proceed but signals missing permissions.
 *
 * @author Maruf Bepary
 */
public class InsufficientScopeException extends RuntimeException {
    
    /**
     * Creates the exception with a human-readable reason for missing scopes.
     *
     * @param message description of the missing permissions
     * @author Maruf Bepary
     */
    public InsufficientScopeException(String message) {
        super(message);
    }
    
    /**
     * Creates the exception and retains the root cause for logging.
     *
     * @param message description of the missing permissions
     * @param cause underlying exception that triggered the failure
     * @author Maruf Bepary
     */
    public InsufficientScopeException(String message, Throwable cause) {
        super(message, cause);
    }
}
