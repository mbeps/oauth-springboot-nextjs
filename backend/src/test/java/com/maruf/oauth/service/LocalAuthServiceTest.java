package com.maruf.oauth.service;

import com.maruf.oauth.entity.User;
import com.maruf.oauth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocalAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LocalAuthService localAuthService;

    @Test
    void registerHashesPasswordAndPersistsUser() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = localAuthService.register("user@example.com", "password", "User");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(created.getEmail()).isEqualTo("user@example.com");
        assertThat(captor.getValue().getPassword()).isEqualTo("hashed");
        assertThat(created.getRoles()).containsExactly("ROLE_USER");
    }

    @Test
    void registerThrowsWhenEmailExists() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> localAuthService.register("user@example.com", "password", "User"));
    }

    @Test
    void loginValidatesPassword() {
        User existing = User.builder().email("user@example.com").password("hashed").build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("password", "hashed")).thenReturn(true);

        assertThat(localAuthService.login("user@example.com", "password")).contains(existing);
        assertThat(localAuthService.login("user@example.com", "wrong")).isEmpty();
    }
}
