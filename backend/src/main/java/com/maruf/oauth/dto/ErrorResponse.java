package com.maruf.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standardized error response for consistent API error messages.
 * Provides error code and human-readable message for client error handling.
 *
 * @author Maruf Bepary
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    /**
     * Machine-readable error code for client logic.
     *
     * @author Maruf Bepary
     */
    private String error;

    /**
     * Human-readable description of the error.
     *
     * @author Maruf Bepary
     */
    private String message;
}
