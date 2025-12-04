package com.maruf.oauth.service;

import com.maruf.oauth.entity.InvalidatedToken;
import com.maruf.oauth.entity.RefreshToken;
import com.maruf.oauth.repository.InvalidatedTokenRepository;
import com.maruf.oauth.repository.RefreshTokenRepository;
import com.maruf.oauth.config.RefreshTokenSecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

/**
 * Coordinates persistence of refresh and invalidated access tokens.
 * Uses MongoDB repositories so token state survives server restarts.
 *
 * @author Maruf Bepary
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenStore {

    private final RefreshTokenRepository refreshTokenRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final RefreshTokenSecurityProperties refreshTokenSecurityProperties;

    /**
     * Saves a refresh token record with creation and usage timestamps.
     * Writes to MongoDB so multiple application instances can share token state.
     *
     * @param token     refresh token string generated for the session
     * @param username  account owner associated with the token
     * @param expiresAt expiration moment tracked for cleanup
     * @author Maruf Bepary
     */
    public void storeRefreshToken(String token, String username, Instant expiresAt) {
        String tokenValue = applyHash(token);
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .username(username)
                .expiresAt(expiresAt)
                .createdAt(Instant.now())
                .lastUsed(Instant.now())
                .build();
        
        refreshTokenRepository.save(refreshToken);
        log.debug("Stored refresh token for user: {}", username);
    }

    /**
     * Looks up the username for a refresh token and updates the last used timestamp.
     * Returns {@code null} when the token is missing so callers can send an HTTP 401.
     *
     * @param token refresh token presented by the client
     * @author Maruf Bepary
     */
    public String getUsernameFromRefreshToken(String token) {
        return findTokenRecord(token)
                .map(refreshToken -> {
                    // Update last used timestamp
                    refreshToken.setLastUsed(Instant.now());
                    refreshTokenRepository.save(refreshToken);
                    return refreshToken.getUsername();
                })
                .orElse(null);
    }

    /**
     * Removes the refresh token entry when a client logs out or presents an invalid token.
     * Using delete avoids storing stale rows that the TTL index might not catch quickly.
     *
     * @param token refresh token value to remove from persistence
     * @author Maruf Bepary
     */
    public void invalidateRefreshToken(String token) {
        String hashedToken = applyHash(token);
        refreshTokenRepository.deleteByToken(hashedToken);
        log.debug("Refresh token invalidated");
    }

    /**
     * Persists an invalidated access token to protect against replay until it naturally expires.
     * Stores the username and reason for auditing.
     *
     * @param token     access token string that should no longer authenticate requests
     * @param username  principal associated with the invalidated token
     * @param expiresAt expiration instant copied from the JWT payload
     * @author Maruf Bepary
     */
    public void invalidateAccessToken(String token, String username, Instant expiresAt) {
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .token(token)
                .username(username)
                .expiresAt(expiresAt)
                .invalidatedAt(Instant.now())
                .reason("logout")
                .build();
        
        invalidatedTokenRepository.save(invalidatedToken);
        log.debug("Access token invalidated");
    }

    /**
     * Checks if an access token has been explicitly invalidated.
     * Allows the authentication filter to short circuit even if the JWT is otherwise valid.
     *
     * @param token access token to check for invalidation records
     * @author Maruf Bepary
     */
    public boolean isAccessTokenInvalidated(String token) {
        return invalidatedTokenRepository.existsByToken(token);
    }

    private String applyHash(String token) {
        if (!refreshTokenSecurityProperties.isHashingEnabled()) {
            return token;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 digest is not available", e);
        }
    }

    private Optional<RefreshToken> findTokenRecord(String token) {
        String hashedToken = applyHash(token);
        return refreshTokenRepository.findByToken(hashedToken);
    }
}
