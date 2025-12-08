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
    /**
     * Human-friendly message about the action outcome.
     *
     * @author Maruf Bepary
     */
    private String message;

    /**
     * Username associated with the action.
     *
     * @author Maruf Bepary
     */
    private String user;

    /**
     * Name of the protected action executed.
     *
     * @author Maruf Bepary
     */
    private String action;

    /**
     * Result description returned by the server.
     *
     * @author Maruf Bepary
     */
    private String result;

    /**
     * Epoch timestamp marking when the action completed.
     *
     * @author Maruf Bepary
     */
    private Long timestamp;
}
