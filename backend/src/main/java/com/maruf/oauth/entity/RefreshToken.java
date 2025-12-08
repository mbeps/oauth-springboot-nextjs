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
 * Stores refresh tokens in MongoDB for sliding session control.
 * Indexed fields support quick lookups and automatic expiry via TTL indexes.
 *
 * @author Maruf Bepary
 */
@Document(collection = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String token;
    
    @Indexed
    private String username;
    
    @Indexed(expireAfter = "0s")
    private Instant expiresAt;
    
    private Instant createdAt;
    
    private Instant lastUsed;
}
