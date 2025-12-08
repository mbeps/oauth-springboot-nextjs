package com.maruf.oauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Payload for local login requests.
 *
 * @author Maruf Bepary
 */
@Data
public class LoginRequest {
    /**
     * User email address used as the login identifier.
     *
     * @author Maruf Bepary
     */
    @NotBlank
    @Email
    private String email;

    /**
     * Plaintext password to validate against the stored hash.
     *
     * @author Maruf Bepary
     */
    @NotBlank
    private String password;
}
