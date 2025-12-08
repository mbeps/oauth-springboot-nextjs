package com.maruf.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the subset of OAuth user profile data required by the frontend.
 * Uses camel case field names that mirror GitHub responses to avoid extra mapping.
 *
 * @author Maruf Bepary
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    /**
     * Provider-specific identifier for the user.
     *
     * @author Maruf Bepary
     */
    private String id;

    /**
     * Username or login handle displayed in the app.
     *
     * @author Maruf Bepary
     */
    private String login;

    /**
     * Display name for UI rendering.
     *
     * @author Maruf Bepary
     */
    private String name;

    /**
     * Primary email address.
     *
     * @author Maruf Bepary
     */
    private String email;

    /**
     * Avatar URL if provided by the identity provider.
     *
     * @author Maruf Bepary
     */
    private String avatarUrl;
}
