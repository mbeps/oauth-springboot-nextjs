package com.maruf.oauth.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class HttpCookieFactory {

    private final CookieSecurityProperties cookieSecurityProperties;

    public String getValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    public ResponseCookie buildTokenCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecurityProperties.isSecure())
                .sameSite(cookieSecurityProperties.getSameSite())
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    public void writeTo(HttpServletResponse response, String name, String value, Duration maxAge) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildTokenCookie(name, value, maxAge).toString());
    }

    public void delete(HttpServletResponse response, String name) {
        writeTo(response, name, "", Duration.ZERO);
    }
}
