package com.maruf.oauth.controller;

import com.maruf.oauth.config.CookieSecurityProperties;
import com.maruf.oauth.config.HttpCookieFactory;
import com.maruf.oauth.config.RefreshTokenSecurityProperties;
import com.maruf.oauth.dto.LoginRequest;
import com.maruf.oauth.entity.User;
import com.maruf.oauth.service.JwtService;
import com.maruf.oauth.service.LocalAuthService;
import com.maruf.oauth.service.RefreshTokenStore;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "app.security.local-auth.enabled=true",
        "jwt.access-token-expiration=900000",
        "jwt.refresh-token-expiration=604800000"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RefreshTokenStore refreshTokenStore;

    @MockBean
    private LocalAuthService localAuthService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        ClientRegistrationRepository clientRegistrationRepository() {
            ClientRegistration github = ClientRegistration.withRegistrationId("github")
                    .clientId("id")
                    .clientSecret("secret")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("http://localhost")
                    .authorizationUri("http://auth")
                    .tokenUri("http://token")
                    .userInfoUri("http://user")
                    .userNameAttributeName("id")
                    .clientName("GitHub")
                    .build();

            ClientRegistration azure = ClientRegistration.withRegistrationId("azure")
                    .clientId("id2")
                    .clientSecret("secret2")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("http://localhost")
                    .authorizationUri("http://auth2")
                    .tokenUri("http://token2")
                    .userInfoUri("http://user2")
                    .userNameAttributeName("sub")
                    .clientName("Microsoft Entra ID")
                    .build();

            return new InMemoryClientRegistrationRepository(github, azure);
        }

        @Bean
        CookieSecurityProperties cookieSecurityProperties() {
            CookieSecurityProperties props = new CookieSecurityProperties();
            props.setSecure(false);
            props.setSameSite("Lax");
            return props;
        }

        @Bean
        HttpCookieFactory httpCookieFactory(CookieSecurityProperties properties) {
            return new HttpCookieFactory(properties);
        }

        @Bean
        RefreshTokenSecurityProperties refreshTokenSecurityProperties() {
            RefreshTokenSecurityProperties props = new RefreshTokenSecurityProperties();
            props.setHashingEnabled(true);
            props.setRotationEnabled(true);
            return props;
        }
    }

    @Test
    void providersEndpointIncludesConfiguredAndLocalProviders() throws Exception {
        mockMvc.perform(get("/api/auth/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].key").value(hasItems("github", "azure", "local")))
                .andExpect(jsonPath("$[*].name").value(hasItems("GitHub", "Microsoft Entra ID", "Email & Password")));
    }

    @Test
    void refreshTokenReturnsUnauthorizedWhenMissingCookie() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("token_missing"));
    }

    @Test
    void refreshTokenRotatesAndIssuesCookies() throws Exception {
        when(refreshTokenStore.getUsernameFromRefreshToken("refresh-token")).thenReturn("user@example.com");
        when(jwtService.isTokenValid("refresh-token")).thenReturn(true);
        when(jwtService.isTokenExpired("refresh-token")).thenReturn(false);

        DefaultClaims claims = new DefaultClaims(Map.of(
                "type", "refresh",
                "id", "1",
                "login", "user@example.com",
                "name", "User Example",
                "email", "user@example.com",
                "avatar_url", "http://example.com/avatar.png"
        ));
        when(jwtService.extractAllClaims("refresh-token")).thenReturn(claims);
        when(jwtService.generateAccessToken(any())).thenReturn("new-access");
        when(jwtService.generateRefreshToken(eq("user@example.com"), anyMap())).thenReturn("new-refresh");

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(stringContainsInOrder("jwt=new-access"))))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(stringContainsInOrder("refresh_token=new-refresh"))));

        verify(refreshTokenStore).invalidateRefreshToken("refresh-token");
        verify(refreshTokenStore).storeRefreshToken(eq("new-refresh"), eq("user@example.com"), any(Instant.class));
    }

    @Test
    void loginIssuesCookiesWhenLocalAuthEnabled() throws Exception {
        User user = User.builder()
                .id("1")
                .email("user@example.com")
                .name("User Example")
                .avatarUrl("http://example.com/avatar.png")
                .build();
        when(localAuthService.login("user@example.com", "password")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(eq("user@example.com"), anyMap())).thenReturn("refresh-token");

        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(request)))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(stringContainsInOrder("jwt=access-token"))))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(stringContainsInOrder("refresh_token=refresh-token"))));

        verify(refreshTokenStore).storeRefreshToken(eq("refresh-token"), eq("user@example.com"), any(Instant.class));
    }

    @Test
    void authStatusReturnsAuthenticatedState() throws Exception {
        mockMvc.perform(get("/api/auth/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false));
    }

    private String asJson(Object value) throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper.writeValueAsString(value);
    }
}
