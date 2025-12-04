package com.maruf.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the response returned after a protected action is performed.
 * Keeps the payload minimal so the frontend can render user feedback without extra mapping.
 *
 * @author Maruf Bepary
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionResponse {
    private String message;
    private String user;
    private String action;
    private String result;
    private Long timestamp;
}