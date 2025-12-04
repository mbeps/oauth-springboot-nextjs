package com.maruf.oauth.service;

import com.maruf.oauth.util.OAuth2AttributeExtractor;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles creation and validation of JWT access and refresh tokens.
 * Centralises signing settings so authentication components stay consistent.
 *
 * @author Maruf Bepary
 */
@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration:900000}") // Default 15 minutes
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}") // Default 7 days
    private Long refreshTokenExpiration;

    /**
     * Builds the HMAC signing key from the configured secret string.
     * Uses UTF-8 encoding to mirror the expectations of {@code io.jsonwebtoken} documentation.
     *
     * @author Maruf Bepary
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a short lived access token populated with selected OAuth attributes.
     * Delegates to {@link #generateToken(OAuth2User, Long, String)} to keep signing logic reusable.
     *
     * @param oauth2User authenticated OAuth2 user providing claims for the access token
     * @author Maruf Bepary
     */
    public String generateAccessToken(OAuth2User oauth2User) {
        return generateToken(oauth2User, accessTokenExpiration, "access");
    }

    /**
     * Creates a refresh token bound to the given username.
     * Marks the token type to simplify downstream validation.
     *
     * @param username unique user identifier used as the JWT subject
     * @author Maruf Bepary
     */
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Internal helper that creates a signed JWT with shared claim population.
     * Accepts an explicit expiration to support both access and refresh token flows.
     *
     * @param oauth2User authenticated user whose attributes become token claims
     * @param expiration lifetime in milliseconds from now for the token
     * @param type       token classification recorded in the {@code type} claim
     * @author Maruf Bepary
     */
    private String generateToken(OAuth2User oauth2User, Long expiration, String type) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", OAuth2AttributeExtractor.getUserId(oauth2User));
        String resolvedUsername = OAuth2AttributeExtractor.resolveUsername(oauth2User);
        claims.put("login", resolvedUsername);
        claims.put("name", OAuth2AttributeExtractor.getName(oauth2User));
        claims.put("email", OAuth2AttributeExtractor.getEmail(oauth2User));
        claims.put("avatar_url", OAuth2AttributeExtractor.getAvatarUrl(oauth2User));
        claims.put("type", type);

        String username = resolvedUsername;
        if (username == null) {
            throw new IllegalStateException("Unable to determine username from OAuth2 user");
        }
        
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Parses the token and returns all claims for further inspection.
     * Uses the new parser API recommended in the JJWT 0.12+ documentation.
     *
     * @param token JWT string to parse and validate
     * @author Maruf Bepary
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Returns the subject claim, which stores the username.
     * Keeps a dedicated method so callers avoid parsing claims twice.
     *
     * @param token JWT string from which to extract the subject claim
     * @author Maruf Bepary
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Reads the custom {@code type} claim to distinguish access and refresh tokens.
     * Supports logging and guards that need to branch on token purpose.
     *
     * @param token JWT string containing the {@code type} claim
     * @author Maruf Bepary
     */
    public String extractTokenType(String token) {
        return (String) extractAllClaims(token).get("type");
    }

    /**
     * Checks if the token signature is valid and not expired.
     * Logs parsing errors to aid debugging while returning a boolean to callers.
     *
     * @param token JWT candidate to validate
     * @author Maruf Bepary
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Determines if the token expiration timestamp lies in the past.
     * Treats parsing issues as expired to fail safe.
     *
     * @param token JWT candidate whose expiry should be inspected
     * @author Maruf Bepary
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractAllClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Extracts the expiration date for logging or cookie management.
     * Exposes the raw {@link Date} so callers can convert to other time types as needed.
     *
     * @param token JWT whose expiration claim should be returned
     * @author Maruf Bepary
     */
    public Date getExpirationDate(String token) {
        return extractAllClaims(token).getExpiration();
    }
}