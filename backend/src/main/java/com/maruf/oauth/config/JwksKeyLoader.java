package com.maruf.oauth.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

/**
 * Component responsible for retrieving the RSA public key published by the
 * authentication service's JWKS endpoint.
 *
 * <p>
 * During application startup ({@link PostConstruct init()}), it performs an
 * HTTP GET to {@code {auth.service.jwksUrl}/.well-known/jwks.json}, parses
 * the first key entry, and constructs an {@link RSAPublicKey}. Failure to load
 * a usable key results in an {@link IllegalStateException} which stops
 * the backend from starting; this is intentional because the API cannot
 * verify tokens without the key.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwksKeyLoader {

	private final AuthServiceProperties authServiceProperties;
	private RSAPublicKey publicKey;

	/**
	 * Lifecycle callback invoked by Spring after construction.
	 * Delegates to {@link #loadPublicKey()}.
	 */
	@PostConstruct
	public void init() {
		loadPublicKey();
	}

	/**
	 * Returns the previously loaded RSA public key.
	 *
	 * @return the public key used for JWT verification
	 */
	public RSAPublicKey getPublicKey() {
		return publicKey;
	}

	/**
	 * Fetches the JWKS JSON, extracts the modulus/exponent (fields {@code n} and
	 * {@code e}), and converts them into an {@link RSAPublicKey} instance.
	 *
	 * <p>
	 * If the response is malformed or the endpoint is unreachable, this
	 * method throws {@link IllegalStateException} which aborts application
	 * initialization.
	 */
	private void loadPublicKey() {
		String jwksUrl = authServiceProperties.getJwksUrl() + "/.well-known/jwks.json";
		log.info("Loading JWKS from: {}", jwksUrl);

		try {
			RestTemplate restTemplate = new RestTemplate();
			String response = restTemplate.getForObject(jwksUrl, String.class);

			ObjectMapper mapper = new ObjectMapper();
			JsonNode jwks = mapper.readTree(response);
			JsonNode keys = jwks.get("keys");

			if (keys == null || !keys.isArray() || keys.isEmpty()) {
				throw new IllegalStateException("No keys found in JWKS response");
			}

			JsonNode firstKey = keys.get(0);
			String n = firstKey.get("n").asText();
			String e = firstKey.get("e").asText();

			byte[] modulusBytes = Base64.getUrlDecoder().decode(n);
			byte[] exponentBytes = Base64.getUrlDecoder().decode(e);

			BigInteger modulus = new BigInteger(1, modulusBytes);
			BigInteger exponent = new BigInteger(1, exponentBytes);

			RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			this.publicKey = (RSAPublicKey) keyFactory.generatePublic(spec);

			log.info("RSA public key loaded successfully from JWKS endpoint");
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to load public key from JWKS endpoint: " + jwksUrl, ex);
		}
	}
}
