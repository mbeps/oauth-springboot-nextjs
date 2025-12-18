package com.maruf.oauth.support;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.Map;

/**
 * Helper factory for constructing {@link OAuth2User} instances in tests
 * without pulling in full OAuth client machinery.
 */
public final class TestOAuth2Users {

    private TestOAuth2Users() {
    }

    public static OAuth2User withAttributes(Map<String, Object> attributes) {
        String nameAttributeKey = resolveNameAttribute(attributes);
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                nameAttributeKey
        );
    }

    private static String resolveNameAttribute(Map<String, Object> attributes) {
        if (attributes.containsKey("login")) {
            return "login";
        }
        if (attributes.containsKey("preferred_username")) {
            return "preferred_username";
        }
        if (attributes.containsKey("email")) {
            return "email";
        }
        if (attributes.containsKey("id")) {
            return "id";
        }
        if (attributes.containsKey("oid")) {
            return "oid";
        }
        if (attributes.containsKey("sub")) {
            return "sub";
        }
        return attributes.keySet().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("At least one attribute is required"));
    }
}
