package com.maruf.oauth.util;

import com.maruf.oauth.exception.InsufficientScopeException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;

/**
 * Provides helpers for reading OAuth2 attributes with predictable types.
 * Normalises common provider claim shapes so controllers stay concise.
 *
 * @author Maruf Bepary
 */
public final class OAuth2AttributeExtractor {

    private OAuth2AttributeExtractor() {
    }

    /**
     * Resolves a stable user identifier across OAuth providers.
     * Prefers GitHub's {@code id} claim, otherwise falls back to Microsoft Entra's {@code oid} or {@code sub}.
     *
     * @param principal authenticated OAuth2 user supplying attribute data
     * @return canonical user identifier or {@code null} if unavailable
     */
    public static String getUserId(OAuth2User principal) {
        Object id = principal.getAttribute("id");
        if (id != null) {
            return id.toString();
        }

        Object oid = principal.getAttribute("oid");
        if (oid != null) {
            return oid.toString();
        }

        Object sub = principal.getAttribute("sub");
        if (sub != null) {
            return sub.toString();
        }

        return null;
    }

    /**
     * Resolves the preferred username or login identifier.
     * Supports GitHub's {@code login} alongside Microsoft Entra's {@code preferred_username} and {@code upn}.
     *
     * @param principal authenticated OAuth2 user supplying attribute data
     * @return canonical username or {@code null} if unavailable
     */
    public static String getLogin(OAuth2User principal) {
        Object login = principal.getAttribute("login");
        if (login != null) {
            return login.toString();
        }

        Object preferredUsername = principal.getAttribute("preferred_username");
        if (preferredUsername != null) {
            return preferredUsername.toString();
        }

        Object upn = principal.getAttribute("upn");
        if (upn != null) {
            return upn.toString();
        }

        Object email = principal.getAttribute("email");
        if (email != null) {
            return email.toString();
        }

        return null;
    }

    /**
     * Resolves a display name.
     * Checks the common {@code name} claim and falls back to {@code displayName}.
     *
     * @param principal authenticated OAuth2 user supplying attribute data
     * @return display name or {@code null} if unavailable
     */
    public static String getName(OAuth2User principal) {
        Object name = principal.getAttribute("name");
        if (name != null) {
            return name.toString();
        }

        Object displayName = principal.getAttribute("displayName");
        if (displayName != null) {
            return displayName.toString();
        }

        return null;
    }

    /**
     * Resolves the primary email address.
     * Handles GitHub's {@code email}, Microsoft Entra's {@code email}, and {@code emails} collection claims.
     *
     * @param principal authenticated OAuth2 user supplying attribute data
     * @return email address or {@code null} if unavailable
     */
    public static String getEmail(OAuth2User principal) {
        Object email = principal.getAttribute("email");
        if (email != null) {
            return email.toString();
        }

        Object preferredUsername = principal.getAttribute("preferred_username");
        if (preferredUsername != null && preferredUsername.toString().contains("@")) {
            return preferredUsername.toString();
        }

        Object emails = principal.getAttribute("emails");
        if (emails instanceof Collection<?>) {
            return ((Collection<?>) emails).stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElse(null);
        }

        return null;
    }

    /**
     * Resolves an avatar or profile picture URL when provided.
     * Supports GitHub's {@code avatar_url} and providers that publish a {@code picture} claim.
     *
     * @param principal authenticated OAuth2 user supplying attribute data
     * @return avatar URL or {@code null} if unavailable
     */
    public static String getAvatarUrl(OAuth2User principal) {
        Object avatarUrl = principal.getAttribute("avatar_url");
        if (avatarUrl != null) {
            return avatarUrl.toString();
        }

        Object picture = principal.getAttribute("picture");
        if (picture != null) {
            return picture.toString();
        }

        return null;
    }

    /**
     * Resolves a stable username for token subjects and persistence.
     * Falls back from login to email and finally to provider specific user identifiers.
     *
     * @param principal authenticated OAuth2 user supplying attribute data
     * @return stable username or {@code null} when all sources are empty
     */
    public static String resolveUsername(OAuth2User principal) {
        String username = getLogin(principal);
        if (username != null) {
            return username;
        }

        username = getEmail(principal);
        if (username != null) {
            return username;
        }

        return getUserId(principal);
    }

    /**
     * Validates that minimum required OAuth attributes are present.
     * Checks for user identifier and login/email - at least one must exist.
     *
     * @param principal authenticated OAuth2 user to validate
     * @throws InsufficientScopeException if required attributes are missing
     * @author Maruf Bepary
     */
    public static void validateRequiredAttributes(OAuth2User principal) {
        String userId = getUserId(principal);
        String login = getLogin(principal);
        
        if (userId == null) {
            throw new InsufficientScopeException("Missing user identifier - OAuth provider did not return user ID");
        }
        
        if (login == null) {
            throw new InsufficientScopeException("Missing username/email - required scope may not have been granted");
        }
    }
}