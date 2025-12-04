package com.maruf.oauth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Captures the action identifier submitted by the user.
 * Validation annotations ensure the backend rejects empty or oversized commands early.
 *
 * @author Maruf Bepary
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionRequest {
    @NotBlank(message = "Action cannot be blank")
    @Size(max = 50, message = "Action must be less than 50 characters")
    private String action;
}