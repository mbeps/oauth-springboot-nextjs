package com.maruf.oauth.controller;

import com.maruf.oauth.config.HttpCookieFactory;
import com.maruf.oauth.config.RefreshTokenSecurityProperties;
import com.maruf.oauth.dto.AuthStatusResponse;
import com.maruf.oauth.dto.ErrorResponse;
import com.maruf.oauth.dto.LoginRequest;
import com.maruf.oauth.dto.SignupRequest;
import com.maruf.oauth.dto.UserResponse;
import com.maruf.oauth.entity.User;
import com.maruf.oauth.service.JwtService;
import com.maruf.oauth.service.LocalAuthService;
import com.maruf.oauth.service.RefreshTokenStore;
import com.maruf.oauth.util.OAuth2AttributeExtractor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Claims;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages authentication lifecycle endpoints such as status checks and token refresh.
 * Coordinates JWT generation with cookie settings to match the Next.js client flow.
 *
 * @author Maruf Bepary
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;
    private final HttpCookieFactory cookieFactory;
    private final RefreshTokenSecurityProperties refreshTokenSecurityProperties;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final LocalAuthService localAuthService;

    /**
     * Flag controlling whether email/password endpoints are exposed.
     * Read from {@code app.security.local-auth.enabled}; defaults to {@code false}.
     *
     * @author Maruf Bepary
     */
    @Value("${app.security.local-auth.enabled:false}")
    private boolean localAuthEnabled;

    /**
     * Access token TTL in milliseconds from {@code jwt.access-token-expiration}; defaults to 15 minutes.
     *
     * @author Maruf Bepary
     */
    @Value("${jwt.access-token-expiration:900000}") 
    private Long accessTokenExpiration;

    /**
     * Refresh token TTL in milliseconds from {@code jwt.refresh-token-expiration}; defaults to 7 days.
     *
     * @author Maruf Bepary
     */
    @Value("${jwt.refresh-token-expiration:604800000}")
    private Long refreshTokenExpiration;

    /**
     * Returns whether the current request is authenticated and, if so, the associated profile.
     * Builds the DTO manually to keep tight control over which OAuth fields leave the server.
     *
     * @param principal the authenticated principal resolved by Spring Security, may be {@code null}
     * @author Maruf Bepary
     */
    @GetMapping("/api/auth/status")
    public ResponseEntity<AuthStatusResponse> getAuthStatus(@AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) { // user is authenticated
            UserResponse user = UserResponse.builder()
                    .id(OAuth2AttributeExtractor.getUserId(principal))
                    .login(OAuth2AttributeExtractor.resolveUsername(principal))
                    .name(OAuth2AttributeExtractor.getName(principal))
                    .email(OAuth2AttributeExtractor.getEmail(principal))
                    .avatarUrl(OAuth2AttributeExtractor.getAvatarUrl(principal))
                    .build();

            log.info("Auth status checked for user: {}", user.getLogin());
            
            return ResponseEntity.ok(
                AuthStatusResponse.builder()
                    .authenticated(true)
                    .user(user)
                    .build()
            );
        } else { // user is not authenticated
            log.info("Auth status checked - user not authenticated");
            return ResponseEntity.ok(
                AuthStatusResponse.builder()
                    .authenticated(false)
                    .build()
            );
        }
    }

    /**
     * Issues a new access token when a valid refresh token cookie is presented.
     * Reuses a minimal {@link OAuth2User} instance so downstream JWT code remains shared with login.
     *
     * @param request  HTTP servlet request containing authentication cookies
     * @param response HTTP servlet response used to publish a renewed access token cookie
     * @author Maruf Bepary
     */
    @PostMapping("/api/auth/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // Extract refresh token from cookie
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            log.warn("Refresh token not found in cookies");
            return ResponseEntity.status(401)
                    .body(ErrorResponse.builder()
                            .error("token_missing")
                            .message("Refresh token not found")
                            .build());
        }

        // Validate refresh token and get username
        String username = refreshTokenStore.getUsernameFromRefreshToken(refreshToken);
        if (username == null) {
            log.warn("Invalid or expired refresh token");
            return ResponseEntity.status(401)
                    .body(ErrorResponse.builder()
                            .error("token_invalid")
                            .message("Invalid or expired refresh token")
                            .build());
        }

        try {
            // Validate the refresh token itself with JWT
            if (!jwtService.isTokenValid(refreshToken) || jwtService.isTokenExpired(refreshToken)) {
                log.warn("Refresh token is invalid or expired");
                refreshTokenStore.invalidateRefreshToken(refreshToken);
                return ResponseEntity.status(401)
                        .body(ErrorResponse.builder()
                                .error("token_expired")
                                .message("Refresh token has expired")
                                .build());
            }

            Claims claims = jwtService.extractAllClaims(refreshToken);
            String tokenType = (String) claims.get("type");
            if (!"refresh".equals(tokenType)) {
                log.warn("Token presented is not a refresh token");
                refreshTokenStore.invalidateRefreshToken(refreshToken);
                return ResponseEntity.status(401)
                        .body(ErrorResponse.builder()
                                .error("token_invalid")
                                .message("Invalid token type")
                                .build());
            }

            // Create a minimal OAuth2User for token generation
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("id", claims.get("id"));
            attributes.put("login", claims.getOrDefault("login", username));
            attributes.put("name", claims.get("name"));
            attributes.put("email", claims.get("email"));
            attributes.put("avatar_url", claims.get("avatar_url"));
            
            OAuth2User oauth2User = new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                    attributes,
                    "login"
            );

            // Generate new access token
            String newAccessToken = jwtService.generateAccessToken(oauth2User);
            
            // Rotate refresh token if enabled
            if (refreshTokenSecurityProperties.isRotationEnabled()) {
                rotateRefreshToken(response, refreshToken, username, attributes);
            }

            // Set new access token as cookie
            addCookie(response, "jwt", newAccessToken, Duration.ofMillis(accessTokenExpiration));

            log.info("Access token refreshed for user: {}", username);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Token refreshed successfully"
            ));
        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ErrorResponse.builder()
                            .error("refresh_failed")
                            .message("Token refresh failed")
                            .build());
        }
    }

    /**
     * Returns a list of configured OAuth2 providers.
     * Used by the frontend to dynamically render login buttons.
     *
     * @return List of provider details (key, name)
     * @author Maruf Bepary
     */
    @GetMapping("/api/auth/providers")
    public ResponseEntity<List<Map<String, String>>> getProviders() {
        List<Map<String, String>> providers = new ArrayList<>();
        if (clientRegistrationRepository instanceof Iterable) {
            Iterable<ClientRegistration> iterable = (Iterable<ClientRegistration>) clientRegistrationRepository;
            iterable.forEach(registration -> {
                Map<String, String> provider = new HashMap<>();
                provider.put("key", registration.getRegistrationId());
                provider.put("name", registration.getClientName());
                providers.add(provider);
            });
        }
        
        if (localAuthEnabled) {
            Map<String, String> localProvider = new HashMap<>();
            localProvider.put("key", "local");
            localProvider.put("name", "Email & Password");
            providers.add(localProvider);
        }
        
        return ResponseEntity.ok(providers);
    }

    /**
     * Registers a new user when local authentication is enabled and issues tokens on success.
     * Returns HTTP 403 if the feature is disabled.
     *
     * @param signupRequest validated signup payload containing email, password, and name
     * @author Maruf Bepary
     */
    @PostMapping("/api/auth/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        if (!localAuthEnabled) {
            return ResponseEntity.status(403).body(ErrorResponse.builder()
                    .error("access_denied")
                    .message("Local authentication is disabled")
                    .build());
        }

        try {
            User user = localAuthService.register(signupRequest.getEmail(), signupRequest.getPassword(), signupRequest.getName());
            return authenticateUser(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.builder()
                    .error("signup_failed")
                    .message(e.getMessage())
                    .build());
        }
    }

    /**
     * Authenticates a local user and issues JWT cookies when credentials match.
     * Returns HTTP 403 when local auth is disabled and 401 when credentials are invalid.
     *
     * @param loginRequest validated login payload containing email and password
     * @author Maruf Bepary
     */
    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        if (!localAuthEnabled) {
            return ResponseEntity.status(403).body(ErrorResponse.builder()
                    .error("access_denied")
                    .message("Local authentication is disabled")
                    .build());
        }

        return localAuthService.login(loginRequest.getEmail(), loginRequest.getPassword())
                .map(this::authenticateUser)
                .orElse(ResponseEntity.status(401).body(ErrorResponse.builder()
                        .error("login_failed")
                        .message("Invalid email or password")
                        .build()));
    }

    /**
     * Issues access and refresh tokens for the given user and persists refresh token state.
     * Builds consistent cookies using the shared {@link HttpCookieFactory}.
     *
     * @param user authenticated user entity
     * @return response containing cookies and a success flag
     * @author Maruf Bepary
     */
    private ResponseEntity<?> authenticateUser(User user) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", user.getId());
        attributes.put("login", user.getEmail());
        attributes.put("name", user.getName());
        attributes.put("email", user.getEmail());
        attributes.put("avatar_url", user.getAvatarUrl());

        OAuth2User oauth2User = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "login"
        );

        String accessToken = jwtService.generateAccessToken(oauth2User);
        String refreshToken = jwtService.generateRefreshToken(user.getEmail(), attributes);

        Instant refreshExpiresAt = Instant.now().plusMillis(refreshTokenExpiration);
        refreshTokenStore.storeRefreshToken(refreshToken, user.getEmail(), refreshExpiresAt);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookieFactory.buildTokenCookie("jwt", accessToken, Duration.ofMillis(accessTokenExpiration)).toString());
        headers.add(HttpHeaders.SET_COOKIE, cookieFactory.buildTokenCookie("refresh_token", refreshToken, Duration.ofMillis(refreshTokenExpiration)).toString());

        return ResponseEntity.ok().headers(headers).body(Map.of("success", true));
    }

    /**
     * Creates an HTTP only cookie aligned with the configured security rules.
     * Centralises cookie flags to avoid divergent settings across authentication responses.
     *
     * @param name   cookie name, typically identifying the token type
     * @param value  token value persisted inside the cookie
     * @param maxAge lifetime before the cookie expires on the client
     * @author Maruf Bepary
     */
    private void addCookie(HttpServletResponse response, String name, String value, Duration maxAge) {
        ResponseCookie cookie = cookieFactory.buildTokenCookie(name, value, maxAge);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * Replaces the presented refresh token with a new one and updates persistence accordingly.
     * Called only when rotation is enabled to enforce single-use refresh tokens.
     *
     * @param response HTTP response used to publish the rotated cookie
     * @param currentRefreshToken existing refresh token to invalidate
     * @param username username associated with the session
     * @param refreshClaims claims to embed in the new refresh token
     * @author Maruf Bepary
     */
    private void rotateRefreshToken(HttpServletResponse response, String currentRefreshToken, String username, Map<String, Object> refreshClaims) {
        refreshTokenStore.invalidateRefreshToken(currentRefreshToken);

        String newRefreshToken = jwtService.generateRefreshToken(username, refreshClaims);
        Instant refreshExpiresAt = Instant.now().plusMillis(refreshTokenExpiration);
        refreshTokenStore.storeRefreshToken(newRefreshToken, username, refreshExpiresAt);
        addCookie(response, "refresh_token", newRefreshToken, Duration.ofMillis(refreshTokenExpiration));

        log.info("Refresh token rotated for user: {}", username);
    }
}
