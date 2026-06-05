package com.maruf.oauth.config;

import com.maruf.oauth.service.JwtService;
import com.maruf.oauth.service.RefreshTokenStore;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;
    private final HttpCookieFactory cookieFactory;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = cookieFactory.getValue(request, CookieNames.JWT);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (refreshTokenStore.isAccessTokenInvalidated(token)) {
                log.warn("Access token is blacklisted: {}", token);
                filterChain.doFilter(request, response);
                return;
            }

            if (jwtService.isTokenValid(token)) {
                String tokenType = jwtService.extractTokenType(token);
                if (!"access".equals(tokenType)) {
                    log.warn("Invalid token type provided as access token: {}", tokenType);
                    filterChain.doFilter(request, response);
                    return;
                }

                String username = jwtService.extractUsername(token);
                Claims claims = jwtService.extractAllClaims(token);
                
                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);
                if (roles == null) roles = Collections.singletonList("ROLE_USER");
                
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                // Reconstruct OAuth2User for compatibility with existing routes
                Map<String, Object> attributes = Map.of(
                        "sub", username,
                        "email", username,
                        "name", claims.get("name", String.class) != null ? claims.get("name", String.class) : username
                );

                OAuth2User oauth2User = new DefaultOAuth2User(authorities, attributes, "sub");

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        oauth2User,
                        null,
                        authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            log.error("Could not set user authentication in security context", e);
        }

        filterChain.doFilter(request, response);
    }
}
