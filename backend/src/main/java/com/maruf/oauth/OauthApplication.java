package com.maruf.oauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstraps the Spring Boot application that backs the OAuth demo.
 * Keeps configuration minimal because security settings live in dedicated config classes.
 *
 * @author Maruf Bepary
 */
@SpringBootApplication
public class OauthApplication {

    /**
     * Starts the embedded server and loads the Spring context.
     * Delegates to {@link SpringApplication#run(Class, String...)} for standard startup behaviour.
     *
     * @param args command-line arguments passed to the application entry point
     * @author Maruf Bepary
     */
    public static void main(String[] args) {
        SpringApplication.run(OauthApplication.class, args);
    }

}
