package com.maruf.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Conveys status information for the public health endpoint.
 * Keeps payload lightweight so it can be cached and monitored easily.
 *
 * @author Maruf Bepary
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicHealthResponse {
    /**
     * Service health indicator, e.g., {@code "UP"}.
     *
     * @author Maruf Bepary
     */
    private String status;

    /**
     * Additional message describing the status.
     *
     * @author Maruf Bepary
     */
    private String message;

    /**
     * Epoch timestamp when the status was generated.
     *
     * @author Maruf Bepary
     */
    private Long timestamp;
}
