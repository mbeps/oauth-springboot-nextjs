package com.maruf.oauth.service;

import com.maruf.oauth.entity.User;
import com.maruf.oauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

/**
 * Handles email/password authentication flows when local auth is enabled.
 * Uses {@link PasswordEncoder} for hashing and persists users in MongoDB.
 *
 * @author Maruf Bepary
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocalAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new local user with hashed password and a default user role.
     * Throws when the email already exists to avoid duplicate accounts.
     *
     * @param email unique email address for the user
     * @param password plaintext password to hash before storage
     * @param name display name stored alongside the account
     * @return persisted {@link User} entity
     * @author Maruf Bepary
     */
    public User register(String email, String password, String name) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name(name)
                .roles(Collections.singletonList("ROLE_USER"))
                .build();

        return userRepository.save(user);
    }

    /**
     * Attempts to authenticate a user via email and password comparison.
     * Returns an {@link Optional} that is empty when credentials are invalid.
     *
     * @param email email address supplied by the client
     * @param password plaintext password to verify against the stored hash
     * @return optional of the authenticated user when credentials match
     * @author Maruf Bepary
     */
    public Optional<User> login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}
