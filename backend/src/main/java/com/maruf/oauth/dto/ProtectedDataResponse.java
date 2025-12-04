package com.maruf.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wraps sensitive data returned to authenticated clients.
 * Separates metadata from payload items so caching rules can differ per field if needed.
 *
 * @author Maruf Bepary
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtectedDataResponse {
    private String message;
    private String user;
    private DataContent data;

    /**
     * Groups the protected items that belong to a single response.
     * Keeps counts and timestamps together to aid client side freshness checks.
     *
     * @author Maruf Bepary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataContent {
        private String[] items;
        private Integer count;
        private Long lastUpdated;
    }
}