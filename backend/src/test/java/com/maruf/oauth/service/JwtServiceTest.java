package com.maruf.oauth.service;

import com.maruf.oauth.support.TestOAuth2Users;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;
    private OAuth2User oauth2User;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret",
                "very-long-test-secret-key-for-jwt-signing-should-be-strong-1234567890");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 3600000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 604800000L);

        oauth2User = TestOAuth2Users.withAttributes(Map.of(
                "id", 1,
                "login", "octocat",
                "name", "Octo Cat",
                "email", "octo@example.com",
                "avatar_url", "https://example.com/avatar.png"
        ));
    }

    @Test
    void generatesValidAccessTokensWithClaims() {
        String token = jwtService.generateAccessToken(oauth2User);

        assertTrue(jwtService.isTokenValid(token));
        Claims claims = jwtService.extractAllClaims(token);
        assertThat(jwtService.extractUsername(token)).isEqualTo("octocat");
        assertThat(jwtService.extractTokenType(token)).isEqualTo("access");
        assertThat(claims.get("email")).isEqualTo("octo@example.com");
        assertThat(claims.get("name")).isEqualTo("Octo Cat");
        assertThat(jwtService.getExpirationDate(token)).isAfter(new java.util.Date());
    }

    @Test
    void generatesRefreshTokensWithCustomClaims() {
        String token = jwtService.generateRefreshToken("octo@example.com", Map.of("id", 1, "login", "octocat"));

        Claims claims = jwtService.extractAllClaims(token);
        assertThat(jwtService.extractUsername(token)).isEqualTo("octo@example.com");
        assertThat(jwtService.extractTokenType(token)).isEqualTo("refresh");
        assertThat(claims.get("login")).isEqualTo("octocat");
    }

    @Test
    void detectsExpiredTokens() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", -1000L);
        String token = jwtService.generateAccessToken(oauth2User);

        assertTrue(jwtService.isTokenExpired(token));
        assertThat(jwtService.isTokenValid(token)).isFalse();
    }
}
