package com.maruf.oauth.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Logs OAuth2 authentication failures with detailed diagnostics and redirects to the frontend.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            log.error("OAuth2 authentication failed: {} - details: {}", oauth2Exception.getError().getErrorCode(),
                    oauth2Exception.getError().getDescription(), oauth2Exception);
        } else {
            log.error("Authentication failed: {}", exception.getMessage(), exception);
        }

        getRedirectStrategy().sendRedirect(request, response,
                frontendUrl + "/?error=auth_failed");
    }
}
