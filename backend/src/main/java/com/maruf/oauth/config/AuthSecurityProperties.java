package com.maruf.oauth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration properties for OAuth2 and CORS security settings.
 * <p>
 * Binds properties from the application YAML file under the {@code auth} prefix
 * to control allowed CORS origins and OAuth2 redirect URI whitelisting. These
 * properties are injected into {@link SecurityConfig},
 * {@link CustomOAuth2AuthorizationRequestResolver},
 * and OAuth2 handlers to enforce security boundaries.
 * <p>
 * <b>Example configuration:</b>
 * 
 * <pre>
 * auth:
 *   allowed-origins:
 *     - http://localhost:3000
 *     - http://localhost:8080
 *   allowed-redirect-urls:
 *     - http://localhost:3000
 * </pre>
 *
 * @author Maruf Bepary
 * @see SecurityConfig
 * @see CustomOAuth2AuthorizationRequestResolver
 * @see OAuth2AuthenticationSuccessHandler
 */
@Component
@ConfigurationProperties(prefix = "auth")
@Data
public class AuthSecurityProperties {
	/**
	 * List of origins permitted for CORS requests.
	 * <p>
	 * Used by Spring Security's {@code CorsConfigurationSource} to validate
	 * incoming requests from other origins.
	 */
	private List<String> allowedOrigins;

	/**
	 * List of base URLs whitelisted for OAuth2 post-login redirect.
	 * <p>
	 * Used by {@link CustomOAuth2AuthorizationRequestResolver} to validate the
	 * {@code redirect_uri} query parameter in initial OAuth2 requests, and by
	 * {@link OAuth2AuthenticationSuccessHandler} to decode and verify the redirect
	 * URL from the OAuth2 state parameter.
	 */
	private List<String> allowedRedirectUrls;
}
