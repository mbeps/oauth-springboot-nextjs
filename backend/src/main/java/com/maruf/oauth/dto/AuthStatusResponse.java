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
    private boolean authenticated;
    private UserResponse user;
}