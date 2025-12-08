package com.maruf.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Provides authentication state feedback to clients polling the session.
 * Combines a boolean flag with optional user info to keep the contract predictable.
 *
 * @author Maruf Bepary
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthStatusResponse {
    /**
     * Indicates whether the requester is authenticated.
     *
     * @author Maruf Bepary
     */
    private boolean authenticated;

    /**
     * Profile details returned when authenticated; {@code null} otherwise.
     *
     * @author Maruf Bepary
     */
    private UserResponse user;
}
