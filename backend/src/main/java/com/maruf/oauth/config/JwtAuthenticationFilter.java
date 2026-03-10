package com.maruf.oauth.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet filter that inspects incoming requests for a JWT stored in the
 * {@code jwt} cookie. If present and valid, it converts the token claims into
 * a {@link OAuth2User} and populates the
 * {@link org.springframework.security.core.context.SecurityContext}
 * so that downstream controllers can use {@code @AuthenticationPrincipal}.
 *
 * <p>
 * The public RSA key used to verify signatures is loaded from the
 * {@link JwksKeyLoader}. Only tokens with a custom claim {@code type=access}
 * are accepted; refresh tokens are intentionally ignored. This service does not
 * consult any blacklist (the auth service holds that data) so invalidation is
 * the responsibility of the auth service.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwksKeyLoader jwksKeyLoader;

    /**
     * Main filter logic executed once per request.
     *
     * <ol>
     * <li>Extract JWT from {@code jwt} cookie.</li>
     * <li>Verify signature and parse claims using the RSA public key.</li>
     * <li>Ensure {@code type} claim equals {@code access}; otherwise skip.</li>
     * <li>Construct an {@link OAuth2User} with minimal authorities and set the
     * authentication in the {@link SecurityContextHolder}.</li>
     * </ol>
     *
     * Errors during parsing are logged at debug/error level but do not prevent
     * the filter chain from proceeding (unauthenticated requests will simply
     * hit security constraints later).
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String jwt = extractJwtFromCookie(request);

        if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(jwksKeyLoader.getPublicKey())
                        .build()
                        .parseSignedClaims(jwt)
                        .getPayload();

                String tokenType = (String) claims.get("type");
                if (!"access".equals(tokenType)) {
                    log.debug("Token is not an access token");
                    filterChain.doFilter(request, response);
                    return;
                }

                Map<String, Object> attributes = extractAttributesFromClaims(claims);

                OAuth2User oauth2User = new DefaultOAuth2User(
                        Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                        attributes,
                        "login");

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        oauth2User,
                        null,
                        oauth2User.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT validated for user: {}", claims.get("login"));
            } catch (Exception e) {
                log.error("JWT validation failed: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Helper to find a cookie named {@code jwt} and return its value.
     *
     * @param request incoming HTTP servlet request
     * @return token string or {@code null} if cookie absent
     */
    private String extractJwtFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Converts selected JWT claims into a map suitable for constructing a
     * {@link OAuth2User}. Numeric IDs are coerced to integer if possible.
     *
     * @param claims JWT claims extracted from the token
     * @return attributes map used by the security framework
     */
    private Map<String, Object> extractAttributesFromClaims(Claims claims) {
        Map<String, Object> attributes = new HashMap<>();

        Object idClaim = claims.get("id");
        if (idClaim instanceof Number) {
            attributes.put("id", ((Number) idClaim).intValue());
        } else if (idClaim != null) {
            attributes.put("id", idClaim);
        }

        addIfNotNull(attributes, "login", claims.get("login"));
        addIfNotNull(attributes, "name", claims.get("name"));
        addIfNotNull(attributes, "email", claims.get("email"));
        addIfNotNull(attributes, "avatar_url", claims.get("avatar_url"));

        return attributes;
    }

    /**
     * Put a value into the attributes map only if it is non-null.
     *
     * @param attributes target map
     * @param key        attribute key
     * @param value      attribute value
     */
    private void addIfNotNull(Map<String, Object> attributes, String key, Object value) {
        if (value != null) {
            attributes.put(key, value);
        }
    }
}
