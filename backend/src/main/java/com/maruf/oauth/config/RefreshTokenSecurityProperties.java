package com.maruf.oauth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds refresh token security toggles from configuration files.
 * Allows environments to opt into hashing and rotation without code changes.
 *
 * @author Maruf Bepary
 */
@Component
@ConfigurationProperties(prefix = "app.security.refresh-token")
@Data
public class RefreshTokenSecurityProperties {

    /**
     * Enables hashing of refresh tokens at rest; defaults to {@code true}.
     *
     * @author Maruf Bepary
     */
    private boolean hashingEnabled = true;

    /**
     * Enables rotation so refresh tokens are single-use; defaults to {@code true}.
     *
     * @author Maruf Bepary
     */
    private boolean rotationEnabled = true;
}
