package com.maruf.oauth.controller;

import com.maruf.oauth.dto.*;
import com.maruf.oauth.util.OAuth2AttributeExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes public and protected API endpoints consumed by the Next.js client.
 * Keeps responses small and log-friendly while delegating attribute parsing to dedicated helpers.
 *
 * @author Maruf Bepary
 */
@RestController
@Slf4j
public class ApiController {

    /**
     * Reports service health for monitoring tools and anonymous callers.
     * Uses a builder so fields stay explicit even as telemetry needs grow.
     *
     * @author Maruf Bepary
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
     * Returns the authenticated user's profile information.
     * Relies on {@link OAuth2AttributeExtractor} to shield the controller from provider specific types.
     *
     * @param principal the authenticated OAuth2 user supplied by Spring Security
     * @author Maruf Bepary
     */
    @GetMapping("/api/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            log.warn("Unauthenticated access attempt to /api/user");
            return ResponseEntity.status(401).build();
        }
        UserResponse response = UserResponse.builder()
                .id(OAuth2AttributeExtractor.getUserId(principal))
                .login(OAuth2AttributeExtractor.resolveUsername(principal))
                .name(OAuth2AttributeExtractor.getName(principal))
                .email(OAuth2AttributeExtractor.getEmail(principal))
                .avatarUrl(OAuth2AttributeExtractor.getAvatarUrl(principal))
                .build();
        
        log.info("User info requested for: {}", response.getLogin());
        return ResponseEntity.ok(response);
    }

    /**
     * Delivers sample protected data to demonstrate JWT guarded requests.
     * Separates user identification from payload generation to simplify future storage integration.
     *
     * @param principal the authenticated OAuth2 user requesting protected content
     * @author Maruf Bepary
     */
    @GetMapping("/api/protected/data")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProtectedDataResponse> getProtectedData(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            log.warn("Unauthenticated access attempt to /api/protected/data");
            return ResponseEntity.status(401).build();
        }
        String username = OAuth2AttributeExtractor.resolveUsername(principal);

        ProtectedDataResponse.DataContent dataContent = ProtectedDataResponse.DataContent.builder()
                .items(new String[]{"Item 1", "Item 2", "Item 3"})
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
     * Handles state changing actions for authenticated users.
     * Validates the incoming payload and echoes a structured response for easy client side notifications.
     *
     * @param principal the authenticated OAuth2 user executing the action
     * @param request   validated request payload describing the action to perform
     * @author Maruf Bepary
     */
    @PostMapping("/api/protected/action")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActionResponse> performAction(
            @AuthenticationPrincipal OAuth2User principal,
            @Validated @RequestBody ActionRequest request) {
        
        if (principal == null) {
            log.warn("Unauthenticated access attempt to /api/protected/action");
            return ResponseEntity.status(401).build();
        }
        String username = OAuth2AttributeExtractor.resolveUsername(principal);
        
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
}