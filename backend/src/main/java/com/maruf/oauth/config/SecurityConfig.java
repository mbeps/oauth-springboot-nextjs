package com.maruf.oauth.config;

import com.maruf.oauth.service.JwtService;
import com.maruf.oauth.service.RefreshTokenStore;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.Arrays;

/**
 * Configures Spring Security for OAuth2 login combined with JWT cookie authentication.
 * Applies stateless session management because tokens carry user identity on each request.
 *
 * @author Maruf Bepary
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;
    private final RefreshTokenStore refreshTokenStore;
    private final JwtService jwtService;
    private final HttpCookieFactory cookieFactory;
    private final OAuth2AuthenticationFailureHandler oauth2FailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Builds the primary security filter chain covering OAuth2 login, JWT filters, and logout handling.
     * Disables server side sessions to rely solely on tokens and enforces cookie cleanup during logout.
     *
     * @param http the mutable {@link HttpSecurity} builder provided by Spring Boot
     * @author Maruf Bepary
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/login", "/error", "/webjars/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/auth/status").permitAll()
                .requestMatchers("/api/auth/providers").permitAll()
                .requestMatchers("/api/auth/refresh").permitAll()
                .requestMatchers("/api/auth/signup").permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/logout").permitAll()
                .requestMatchers("/api/protected/**").authenticated()
                .requestMatchers("/api/user").authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(apiAuthenticationEntryPoint())
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oauth2SuccessHandler)
                .failureHandler(oauth2FailureHandler)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    // Invalidate tokens with proper error handling
                    if (request.getCookies() != null) {
                        for (Cookie cookie : request.getCookies()) {
                            try {
                                if ("jwt".equals(cookie.getName())) {
                                    // Validate token before extracting data
                                    String token = cookie.getValue();
                                    if (jwtService.isTokenValid(token) && !jwtService.isTokenExpired(token)) {
                                        java.time.Instant expiresAt = jwtService.getExpirationDate(token).toInstant();
                                        String username = jwtService.extractUsername(token);
                                        refreshTokenStore.invalidateAccessToken(token, username, expiresAt);
                                    }
                                } else if ("refresh_token".equals(cookie.getName())) {
                                    refreshTokenStore.invalidateRefreshToken(cookie.getValue());
                                }
                            } catch (Exception e) {
                                // Log but don't fail logout if token invalidation fails
                                log.warn("Failed to invalidate token during logout: {}", e.getMessage());
                            }
                        }
                    }
                    
                    // Delete JWT cookie
                    writeCookie(response, "jwt", "", Duration.ZERO);
                    
                    // Delete refresh token cookie
                    writeCookie(response, "refresh_token", "", Duration.ZERO);
                    
                    // Return JSON response
                    response.setStatus(200);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\":true,\"message\":\"Logout successful\"}");
                })
                .deleteCookies("jwt", "refresh_token")
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Defines CORS settings that allow the Next.js frontend to call the API.
     * Restricts origins and headers to reduce attack surface while supporting required HTTP verbs.
     *
     * @author Maruf Bepary
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Creates an authentication entry point that returns 401 for API endpoints.
     * Prevents redirects to login page for AJAX/API calls, improving REST API behavior.
     *
     * @author Maruf Bepary
     */
    @Bean
    public AuthenticationEntryPoint apiAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            String requestUri = request.getRequestURI();
            
            // For API endpoints, return 401 JSON response
            if (requestUri != null && requestUri.startsWith("/api/")) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
            } else {
                // For non-API endpoints, redirect to login
                response.sendRedirect("/login");
            }
        };
    }

    /**
     * Creates a cookie with security defaults for logout responses.
     * Sets {@code maxAge} to zero to instruct browsers to remove the cookie immediately.
     *
     * @param name  cookie identifier to overwrite or delete
     * @param value new cookie value, {@code null} clears the cookie
     * @author Maruf Bepary
     */
    private void writeCookie(HttpServletResponse response, String name, String value, Duration maxAge) {
        ResponseCookie cookie = cookieFactory.buildTokenCookie(name, value, maxAge);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
