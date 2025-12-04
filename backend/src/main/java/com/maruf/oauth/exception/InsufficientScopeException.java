package com.maruf.oauth.exception;

/**
 * Thrown when required OAuth scopes were not granted by the user.
 * Allows authentication to proceed but signals missing permissions.
 *
 * @author Maruf Bepary
 */
public class InsufficientScopeException extends RuntimeException {
    
    public InsufficientScopeException(String message) {
        super(message);
    }
    
    public InsufficientScopeException(String message, Throwable cause) {
        super(message, cause);
    }
}
