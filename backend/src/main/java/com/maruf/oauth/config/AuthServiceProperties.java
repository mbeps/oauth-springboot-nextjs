package com.maruf.oauth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties bound from {@code auth.service.*} keys.
 *
 * <p>
 * Only one property is defined: {@code jwksUrl} which indicates the base
 * URL where the authentication service exposes its JWKS endpoint. The default
 * value points to {@code http://localhost:8081} for local development.
 * {@link JwksKeyLoader} consumes this bean during startup.
 */
@Component
@ConfigurationProperties(prefix = "auth.service")
@Data
public class AuthServiceProperties {
	/**
	 * Base URL of the authentication service JWKS endpoint.
	 *
	 * <p>
	 * The {@link JwksKeyLoader} appends {@code /.well-known/jwks.json} to this
	 * value when fetching the RSA public key at startup. Overridable via
	 * {@code auth.service.jwks-url} in {@code application.yaml}.
	 *
	 * <p>
	 * Default: {@code http://localhost:8081} (auth service on port 8081 in dev).
	 */
	private String jwksUrl = "http://localhost:8081";
}
