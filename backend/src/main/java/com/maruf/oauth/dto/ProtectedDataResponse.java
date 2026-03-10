package com.maruf.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wraps sensitive data returned to authenticated clients.
 * Separates metadata from payload items so caching rules can differ per field
 * if needed.
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
     * Container for paginated or list-based data returned by protected endpoints.
     * Separates the actual items from metadata (count, timestamp) so that caching
     * and update strategies can be applied independently if needed.
     *
     * <p>
     * This inner class is instantiated by the backend's protected endpoints
     * (e.g., {@code GET /api/protected/data}) to wrap the response payload.
     *
     * @author Maruf Bepary
     * @see ProtectedDataResponse
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataContent {
        /**
         * Array of data items returned from the protected endpoint.
         * The exact content depends on the specific endpoint; typically strings
         * or object IDs, but can be any serializable data.
         */
        private String[] items;

        /**
         * Total count of items in the collection. Useful for pagination or
         * truncation logic on the frontend when the full item list is
         * not returned.
         */
        private Integer count;

        /**
         * Unix epoch milliseconds timestamp indicating when the underlying data
         * was last refreshed or updated. Allows clients to determine if a cached
         * copy is stale.
         */
        private Long lastUpdated;
    }
}
