package com.maruf.oauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Payload for creating a local account when local auth is enabled.
 *
 * @author Maruf Bepary
 */
@Data
public class SignupRequest {
    /**
     * Unique email address used as username.
     *
     * @author Maruf Bepary
     */
    @NotBlank
    @Email
    private String email;

    /**
     * Plaintext password that will be hashed server side.
     *
     * @author Maruf Bepary
     */
    @NotBlank
    private String password;

    /**
     * Display name stored on the user profile.
     *
     * @author Maruf Bepary
     */
    @NotBlank
    private String name;
}
