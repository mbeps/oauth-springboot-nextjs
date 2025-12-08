package com.maruf.oauth.config;

import com.maruf.oauth.exception.InsufficientScopeException;
import com.maruf.oauth.service.JwtService;
import com.maruf.oauth.service.RefreshTokenStore;
import com.maruf.oauth.util.OAuth2AttributeExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles the OAuth2 login success flow by issuing access and refresh tokens as cookies.
 * Bridges Spring Security's OAuth2 support with the JWT based session strategy used by the frontend.
 *
 * @author Maruf Bepary
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;
    private final HttpCookieFactory cookieFactory;

    /**
     * Frontend base URL loaded from {@code frontend.url}; defaults to {@code http://localhost:3000}.
     *
     * @author Maruf Bepary
     */
    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Access token lifetime in milliseconds from {@code jwt.access-token-expiration}; default 15 minutes.
     *
     * @author Maruf Bepary
     */
    @Value("${jwt.access-token-expiration:900000}") 
    private Long accessTokenExpiration;

    /**
     * Refresh token lifetime in milliseconds from {@code jwt.refresh-token-expiration}; default 7 days.
     *
     * @author Maruf Bepary
     */
    @Value("${jwt.refresh-token-expiration:604800000}") 
    private Long refreshTokenExpiration;

    /**
     * Called after OAuth2 login succeeds to generate and persist tokens, then redirect the user.
     * Stores refresh tokens in MongoDB so logouts from one device invalidate sessions elsewhere.
     *
     * @param request        HTTP request originating from the OAuth2 callback
     * @param response       HTTP response used for cookie delivery and redirect handling
     * @param authentication completed authentication containing the OAuth2 principal
     * @author Maruf Bepary
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException {
        
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        
        // Validate minimum required attributes are present
        try {
            OAuth2AttributeExtractor.validateRequiredAttributes(oauth2User);
        } catch (InsufficientScopeException e) {
            log.warn("OAuth scope validation failed: {}", e.getMessage());
            getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/?error=missing_scope");
            return;
        }
        
        String username = OAuth2AttributeExtractor.resolveUsername(oauth2User);
        if (username == null) {
            log.error("Unable to determine username from OAuth2 attributes: {}", oauth2User.getAttributes());
            getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/?error=missing_profile");
            return;
        }
        
        // Generate access token (short-lived)
        String accessToken = jwtService.generateAccessToken(oauth2User);
        
        // Generate refresh token (long-lived)
        Map<String, Object> refreshClaims = new HashMap<>();
        refreshClaims.put("id", OAuth2AttributeExtractor.getUserId(oauth2User));
        refreshClaims.put("login", username);
        refreshClaims.put("name", OAuth2AttributeExtractor.getName(oauth2User));
        refreshClaims.put("email", OAuth2AttributeExtractor.getEmail(oauth2User));
        refreshClaims.put("avatar_url", OAuth2AttributeExtractor.getAvatarUrl(oauth2User));
        String refreshToken = jwtService.generateRefreshToken(username, refreshClaims);
        
        // Store refresh token
        Instant refreshExpiresAt = Instant.now().plusMillis(refreshTokenExpiration);
        refreshTokenStore.storeRefreshToken(refreshToken, username, refreshExpiresAt);
        
        // Set access token as httpOnly cookie
        addCookie(response, "jwt", accessToken, Duration.ofMillis(accessTokenExpiration));
        
        // Set refresh token as httpOnly cookie
        addCookie(response, "refresh_token", refreshToken, Duration.ofMillis(refreshTokenExpiration));
        
        log.info("Access and refresh tokens generated for user: {}", username);
        
        // Redirect to frontend dashboard
        getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/dashboard");
    }

    /**
     * Builds a cookie with security attributes aligned to application settings.
     * Centralises cookie creation to keep the login and refresh handlers consistent.
     *
     * @param name   cookie name to create, such as {@code jwt} or {@code refresh_token}
     * @param value  encoded token payload placed in the cookie value
     * @param maxAge lifetime before the cookie expires in the browser
     * @author Maruf Bepary
     */
    private void addCookie(HttpServletResponse response, String name, String value, Duration maxAge) {
        ResponseCookie cookie = cookieFactory.buildTokenCookie(name, value, maxAge);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
