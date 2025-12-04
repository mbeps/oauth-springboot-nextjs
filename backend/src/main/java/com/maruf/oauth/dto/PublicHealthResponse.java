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
    private String status;
    private String message;
    private Long timestamp;
}