package com.maruf.oauth.service;

import com.maruf.oauth.config.RefreshTokenSecurityProperties;
import com.maruf.oauth.entity.InvalidatedToken;
import com.maruf.oauth.entity.RefreshToken;
import com.maruf.oauth.repository.InvalidatedTokenRepository;
import com.maruf.oauth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import({RefreshTokenStore.class, RefreshTokenSecurityProperties.class})
@ActiveProfiles("test")
class RefreshTokenStoreTest {

    @Autowired
    private RefreshTokenStore refreshTokenStore;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private InvalidatedTokenRepository invalidatedTokenRepository;

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
        invalidatedTokenRepository.deleteAll();
    }

    @Test
    void storesHashesAndRetrievesRefreshTokens() {
        String rawToken = "sample-refresh-token";
        Instant expiresAt = Instant.now().plusSeconds(60);

        refreshTokenStore.storeRefreshToken(rawToken, "user@example.com", expiresAt);

        List<RefreshToken> tokens = refreshTokenRepository.findAll();
        assertThat(tokens).hasSize(1);
        RefreshToken saved = tokens.getFirst();
        assertThat(saved.getToken()).isNotEqualTo(rawToken); // hashing enabled by default
        assertThat(saved.getUsername()).isEqualTo("user@example.com");
        assertThat(saved.getLastUsed()).isNotNull();

        String username = refreshTokenStore.getUsernameFromRefreshToken(rawToken);
        assertThat(username).isEqualTo("user@example.com");
        RefreshToken updated = refreshTokenRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getLastUsed()).isAfterOrEqualTo(saved.getLastUsed());
    }

    @Test
    void invalidatesRefreshTokens() {
        String rawToken = "token-to-invalidate";
        refreshTokenStore.storeRefreshToken(rawToken, "user@example.com", Instant.now().plusSeconds(120));

        refreshTokenStore.invalidateRefreshToken(rawToken);

        assertThat(refreshTokenRepository.findAll()).isEmpty();
    }

    @Test
    void marksAccessTokensAsInvalidated() {
        Instant expiresAt = Instant.now().plusSeconds(120);
        refreshTokenStore.invalidateAccessToken("access-token", "user@example.com", expiresAt);

        List<InvalidatedToken> records = invalidatedTokenRepository.findAll();
        assertThat(records).hasSize(1);
        assertThat(refreshTokenStore.isAccessTokenInvalidated("access-token")).isTrue();
    }
}
