package com.maruf.oauth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds cookie security settings from configuration files.
 * Allows toggling flags like {@code secure} per environment without code changes.
 *
 * @author Maruf Bepary
 */
@Component
@ConfigurationProperties(prefix = "cookie")
@Data
public class CookieSecurityProperties {
    
    private boolean secure = false;
    private String sameSite = "Lax";
}