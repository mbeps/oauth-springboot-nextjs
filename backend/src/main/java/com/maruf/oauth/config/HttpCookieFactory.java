package com.maruf.oauth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Centralises construction of HTTP cookies so security flags remain consistent.
 * Reads settings from {@link CookieSecurityProperties} to honour environment specific toggles.
 *
 * @author Maruf Bepary
 */
@Component
@RequiredArgsConstructor
public class HttpCookieFactory {

    private final CookieSecurityProperties cookieSecurityProperties;

    /**
     * Builds a cookie for token storage with the configured security attributes.
     *
     * @param name   cookie name, e.g. {@code jwt} or {@code refresh_token}
     * @param value  cookie payload to deliver to the browser
     * @param maxAge lifetime before the cookie expires client-side
     * @return fully configured {@link ResponseCookie}
     * @author Maruf Bepary
     */
    public ResponseCookie buildTokenCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecurityProperties.isSecure())
                .sameSite(cookieSecurityProperties.getSameSite())
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
