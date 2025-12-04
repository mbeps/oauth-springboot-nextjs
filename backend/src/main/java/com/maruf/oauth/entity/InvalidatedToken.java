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
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String token;
    
    private String username;
    
    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;
    
    private Instant invalidatedAt;
    
    private String reason;
}