package com.maruf.oauth.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class HttpCookieFactoryTest {

    @Test
    void buildsTokenCookiesWithSecurityFlags() {
        CookieSecurityProperties props = new CookieSecurityProperties();
        props.setSecure(true);
        props.setSameSite("Strict");

        HttpCookieFactory factory = new HttpCookieFactory(props);
        ResponseCookie cookie = factory.buildTokenCookie("jwt", "token", Duration.ofMinutes(15));

        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("Strict");
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofMinutes(15));
        assertThat(cookie.getPath()).isEqualTo("/");
    }
}
