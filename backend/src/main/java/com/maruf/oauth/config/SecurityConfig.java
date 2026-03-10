package com.maruf.oauth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security configuration for the backend API.
 *
 * <p>
 * The filter chain defined here enforces a stateless, token-based security
 * model. CORS is restricted to the frontend origin (configured via
 * {@code frontend.url}), CSRF protection is disabled for simplicity, and the
 * session management is set to {@code STATELESS} because all authentication
 * state is conveyed by the access token cookie.
 *
 * <p>
 * Public endpoints under <code>/api/public/**</code> are permitted for
 * everyone while all other <code>/api/**</code> paths require a valid JWT.
 * A custom {@link AuthenticationEntryPoint} returns JSON 401 responses instead
 * of a default login page. The {@link JwtAuthenticationFilter} is registered
 * before the standard {@link UsernamePasswordAuthenticationFilter} to
 * populate the {@link org.springframework.security.core.Authentication}
 * from the token.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Builds the security filter chain used by Spring Security.
     *
     * @param http the {@link HttpSecurity} builder
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(apiAuthenticationEntryPoint()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Creates a CORS configuration source allowing the configured frontend origin
     * with credentials and common methods/headers. Necessary for the browser to
     * send cookies across domains.
     *
     * @return the {@link CorsConfigurationSource} instance
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(frontendUrl));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Produces an {@link AuthenticationEntryPoint} that writes a simple JSON
     * payload when authentication is required but missing/invalid. This replaces
     * the default redirect to a login page which is inappropriate for a JSON API.
     *
     * @return the entry point handling unauthenticated requests
     */
    @Bean
    public AuthenticationEntryPoint apiAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
        };
    }
}
