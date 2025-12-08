package com.maruf.oauth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Records access tokens that should no longer be trusted.
 * Persists invalidation reason and expiry so the filter can reject replay attempts.
 *
 * @author Maruf Bepary
 */
@Document(collection = "invalidated_access_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvalidatedToken {
    
    /**
     * Internal identifier for the invalidated token record.
     *
     * @author Maruf Bepary
     */
    @Id
    private String id;
    
    /**
     * Access token string that should be rejected on future requests.
     *
     * @author Maruf Bepary
     */
    @Indexed(unique = true)
    private String token;
    
    /**
     * Username associated with the invalidated token.
     *
     * @author Maruf Bepary
     */
    private String username;
    
    /**
     * Expiration timestamp used by MongoDB TTL to purge the record when the token would naturally expire.
     *
     * @author Maruf Bepary
     */
    @Indexed(expireAfter = "0s")
    private Instant expiresAt;
    
    /**
     * Time when the token was explicitly invalidated.
     *
     * @author Maruf Bepary
     */
    private Instant invalidatedAt;
    
    /**
     * Reason for invalidation, e.g., logout or manual revoke.
     *
     * @author Maruf Bepary
     */
    private String reason;
}
