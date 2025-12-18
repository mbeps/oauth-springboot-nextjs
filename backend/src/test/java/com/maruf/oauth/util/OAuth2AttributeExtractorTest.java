package com.maruf.oauth.util;

import com.maruf.oauth.exception.InsufficientScopeException;
import com.maruf.oauth.support.TestOAuth2Users;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OAuth2AttributeExtractorTest {

    @Test
    void resolvesIdsFromMultipleProviders() {
        OAuth2User githubUser = TestOAuth2Users.withAttributes(Map.of("id", 123));
        OAuth2User azureUser = TestOAuth2Users.withAttributes(Map.of("oid", "abc-123"));
        OAuth2User fallbackUser = TestOAuth2Users.withAttributes(Map.of("sub", "subject-1"));

        assertThat(OAuth2AttributeExtractor.getUserId(githubUser)).isEqualTo("123");
        assertThat(OAuth2AttributeExtractor.getUserId(azureUser)).isEqualTo("abc-123");
        assertThat(OAuth2AttributeExtractor.getUserId(fallbackUser)).isEqualTo("subject-1");
    }

    @Test
    void resolvesLoginWithProviderFallbacks() {
        OAuth2User githubUser = TestOAuth2Users.withAttributes(Map.of("login", "octocat"));
        OAuth2User azurePreferred = TestOAuth2Users.withAttributes(Map.of("preferred_username", "user@contoso.com"));
        OAuth2User azureUpn = TestOAuth2Users.withAttributes(Map.of("upn", "upn-user"));
        OAuth2User emailFallback = TestOAuth2Users.withAttributes(Map.of("email", "email@example.com"));

        assertThat(OAuth2AttributeExtractor.getLogin(githubUser)).isEqualTo("octocat");
        assertThat(OAuth2AttributeExtractor.getLogin(azurePreferred)).isEqualTo("user@contoso.com");
        assertThat(OAuth2AttributeExtractor.getLogin(azureUpn)).isEqualTo("upn-user");
        assertThat(OAuth2AttributeExtractor.getLogin(emailFallback)).isEqualTo("email@example.com");
    }

    @Test
    void resolvesNameAndAvatarAndEmailVariants() {
        OAuth2User userWithName = TestOAuth2Users.withAttributes(Map.of("name", "Jane Doe"));
        OAuth2User userWithDisplayName = TestOAuth2Users.withAttributes(Map.of("displayName", "John Smith"));
        OAuth2User userWithEmailsCollection = TestOAuth2Users.withAttributes(Map.of("emails", List.of("a@example.com", "b@example.com")));
        OAuth2User userWithAvatarAndPicture = TestOAuth2Users.withAttributes(Map.of("picture", "http://example.com/pic.png"));

        assertThat(OAuth2AttributeExtractor.getName(userWithName)).isEqualTo("Jane Doe");
        assertThat(OAuth2AttributeExtractor.getName(userWithDisplayName)).isEqualTo("John Smith");
        assertThat(OAuth2AttributeExtractor.getEmail(userWithEmailsCollection)).isEqualTo("a@example.com");
        assertThat(OAuth2AttributeExtractor.getAvatarUrl(userWithAvatarAndPicture)).isEqualTo("http://example.com/pic.png");
    }

    @Test
    void resolveUsernameFallsBackFromLoginToEmailToId() {
        OAuth2User loginUser = TestOAuth2Users.withAttributes(Map.of("login", "octocat", "id", "123"));
        OAuth2User emailUser = TestOAuth2Users.withAttributes(Map.of("email", "user@example.com", "id", "234"));
        OAuth2User idOnlyUser = TestOAuth2Users.withAttributes(Map.of("id", "345"));

        assertThat(OAuth2AttributeExtractor.resolveUsername(loginUser)).isEqualTo("octocat");
        assertThat(OAuth2AttributeExtractor.resolveUsername(emailUser)).isEqualTo("user@example.com");
        assertThat(OAuth2AttributeExtractor.resolveUsername(idOnlyUser)).isEqualTo("345");
    }

    @Test
    void validateRequiredAttributesThrowsWhenMissing() {
        OAuth2User missingLogin = TestOAuth2Users.withAttributes(Map.of("id", "123"));
        OAuth2User missingId = TestOAuth2Users.withAttributes(Map.of("login", "user"));

        assertThrows(InsufficientScopeException.class, () -> OAuth2AttributeExtractor.validateRequiredAttributes(missingId));
        assertThrows(InsufficientScopeException.class, () -> OAuth2AttributeExtractor.validateRequiredAttributes(missingLogin));
    }
}
