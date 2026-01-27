package com.ticketmaster.auth.service;


import com.ticketmaster.auth.dto.request.AuthenticationRequest;
import com.ticketmaster.auth.dto.request.RegisterRequest;
import com.ticketmaster.auth.dto.response.AuthenticationResponse;
import com.ticketmaster.auth.entity.User;
import com.ticketmaster.auth.repository.UserRepository;
import com.ticketmaster.auth.util.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void register_shouldReturnToken_whenRequestIsValid() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("example@email.com")
                .password("password")
                .build();

        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(jwtService.generateToken(anyMap(), any(User.class))).thenReturn("jwtToken");

        // Act
        AuthenticationResponse response = authenticationService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertEquals("John", saved.getFirstName());
        assertEquals("Doe", saved.getLastName());
        assertEquals("example@email.com", saved.getEmail());
        assertEquals("encodedPassword", saved.getPassword());
        assertEquals(Role.USER, saved.getRole());

        verify(jwtService).generateToken(anyMap(), eq(saved));
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void register_shouldThrowException_whenRepositoryFails() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("example@email.com")
                .password("password")
                .build();

        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        doThrow(new RuntimeException("Email already exists"))
                .when(userRepository)
                .save(any(User.class));

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authenticationService.register(request));
        assertEquals("Email already exists", ex.getMessage());

        verify(userRepository).save(any(User.class));
        verifyNoInteractions(jwtService);
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void authenticate_shouldReturnToken_whenCredentialsAreValid() {
        // Arrange
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("example@email.com")
                .password("password")
                .build();

        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("example@email.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(anyMap(), any(User.class))).thenReturn("jwtToken");

        // Act
        AuthenticationResponse response = authenticationService.authenticate(request);

        // Assert
        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("example@email.com");
        verify(jwtService).generateToken(anyMap(), eq(user));

        verify(userRepository, never()).save(any(User.class));
        verifyNoMoreInteractions(authenticationManager, userRepository, jwtService);
    }

    @Test
    void authenticate_shouldThrow_whenUserNotFound() {
        // Arrange
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("missing@email.com")
                .password("password")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // AuthenticationManager auth succeeds, but user lookup fails
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        // Act + Assert
        assertThrows(RuntimeException.class, () -> authenticationService.authenticate(request));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("missing@email.com");
        verifyNoInteractions(jwtService);
    }

    @Test
    void authenticate_shouldThrow_whenAuthenticationFails_evenIfUserExists() {
        // Arrange
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("found@email.com")
                .password("wrong-password")
                .build();

        // Even if the user would exist in the DB...
        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("found@email.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        // NOTE: we intentionally do NOT stub userRepository here.
        // AuthenticationService.authenticate() should fail fast at AuthenticationManager,
        // so the repository must not be called.

        // ...but authentication fails first
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act + Assert
        assertThrows(BadCredentialsException.class, () -> authenticationService.authenticate(request));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(jwtService);
    }
}
