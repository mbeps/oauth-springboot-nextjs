package com.maruf.oauth.controller;

import com.maruf.oauth.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Primary REST controller for the backend API.
 *
 * <p>
 * Defines a simple public health check and several protected endpoints that
 * require a valid JWT obtained from the auth service. The controller methods
 * extract user information via the {@code @AuthenticationPrincipal} annotation
 * which relies on {@link com.maruf.oauth.config.JwtAuthenticationFilter}
 * populating the security
 * context.
 */
@RestController
@Slf4j
public class ApiController {

    /**
     * GET /api/public/health
     * <p>
     * Unauthenticated public health endpoint used by the frontend to verify
     * that the backend is reachable. Returns a {@link PublicHealthResponse}
     * containing status, message, and timestamp. No authentication required.
     *
     * @return 200 OK with health payload
     */
    @GetMapping("/api/public/health")
    public ResponseEntity<PublicHealthResponse> publicHealth() {
        PublicHealthResponse response = PublicHealthResponse.builder()
                .status("OK")
                .message("Public endpoint is working")
                .timestamp(System.currentTimeMillis())
                .build();

        log.info("Public health endpoint accessed");
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/user
     * <p>
     * Returns basic profile information about the currently authenticated
     * user. Requires a valid JWT; if the security context is not populated an
     * authentication error will be generated before this method is invoked.
     *
     * @param principal the {@link OAuth2User} extracted from the token
     * @return 200 OK with {@link UserResponse}
     */
    @GetMapping("/api/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getUser(@AuthenticationPrincipal OAuth2User principal) {
        UserResponse response = UserResponse.builder()
                .id(getStringAttribute(principal, "id"))
                .login(getStringAttribute(principal, "login"))
                .name(getStringAttribute(principal, "name"))
                .email(getStringAttribute(principal, "email"))
                .avatarUrl(getStringAttribute(principal, "avatar_url"))
                .build();

        log.info("User info requested for: {}", response.getLogin());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/protected/data
     * <p>
     * Example protected resource. Demonstrates use of
     * {@code @PreAuthorize("isAuthenticated()")} to guard the endpoint. The
     * response includes a static list of items and the username who requested
     * it.
     *
     * @param principal current authenticated principal
     * @return 200 OK with {@link ProtectedDataResponse}
     */
    @GetMapping("/api/protected/data")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProtectedDataResponse> getProtectedData(@AuthenticationPrincipal OAuth2User principal) {
        String username = getStringAttribute(principal, "login");

        ProtectedDataResponse.DataContent dataContent = ProtectedDataResponse.DataContent.builder()
                .items(new String[] { "Item 1", "Item 2", "Item 3" })
                .count(3)
                .lastUpdated(System.currentTimeMillis())
                .build();

        ProtectedDataResponse response = ProtectedDataResponse.builder()
                .message("This is protected data")
                .user(username)
                .data(dataContent)
                .build();

        log.info("Protected data accessed by: {}", username);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/protected/action
     * <p>
     * Accepts an {@link ActionRequest} body and echoes back an
     * {@link ActionResponse} including the authenticated user and the action
     * provided. This endpoint is protected and validates the request body using
     * Jakarta Bean Validation (triggered by {@code @Validated}).
     *
     * @param principal authenticated user
     * @param request   validated action request payload
     * @return 200 OK with {@link ActionResponse}
     */
    @PostMapping("/api/protected/action")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActionResponse> performAction(
            @AuthenticationPrincipal OAuth2User principal,
            @Validated @RequestBody ActionRequest request) {

        String username = getStringAttribute(principal, "login");

        ActionResponse response = ActionResponse.builder()
                .message("Action performed successfully")
                .user(username)
                .action(request.getAction())
                .result("Success")
                .timestamp(System.currentTimeMillis())
                .build();

        log.info("Action '{}' performed by: {}", request.getAction(), username);
        return ResponseEntity.ok(response);
    }

    /**
     * Utility that safely retrieves an attribute from the OAuth2 principal and
     * converts it to a string, returning {@code null} if absent.
     *
     * @param principal OAuth2 user
     * @param key       attribute name in the token
     * @return string value or {@code null}
     */
    private String getStringAttribute(OAuth2User principal, String key) {
        Object value = principal.getAttribute(key);
        return value != null ? value.toString() : null;
    }
}
