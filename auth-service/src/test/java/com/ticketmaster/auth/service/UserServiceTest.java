package com.ticketmaster.auth.service;


import com.ticketmaster.auth.entity.User;
import com.ticketmaster.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void changePasswordSuccessfully() {

        String currentPassword = "oldPassword";
        String newPassword = "newPassword";
        String encodedCurrentPassword = "encodedOldPassword";
        String encodedNewPassword = "encodedNewPassword";

        User user = new User();
        user.setPassword(encodedCurrentPassword);

        UsernamePasswordAuthenticationToken connectedUser = mock(UsernamePasswordAuthenticationToken.class);
        when(connectedUser.getPrincipal()).thenReturn(user);

        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        userService.changePassword(connectedUser, currentPassword, newPassword);

        assertEquals(encodedNewPassword, user.getPassword());
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(user);
    }

    @Test
    void throwExceptionWhenCurrentPasswordIsWrong() {

        String currentPassword = "wrongPassword";
        String newPassword = "newPassword";
        String encodedCurrentPassword = "encodedOldPassword";

        User user = new User();
        user.setPassword(encodedCurrentPassword);

        UsernamePasswordAuthenticationToken connectedUser = mock(UsernamePasswordAuthenticationToken.class);
        when(connectedUser.getPrincipal()).thenReturn(user);

        when(passwordEncoder.matches(currentPassword, user.getPassword())).thenReturn(false);

        var ex = assertThrows(IllegalStateException.class,
                () -> userService.changePassword(connectedUser, currentPassword, newPassword));

        assertEquals("Wrong password", ex.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldNotChangePasswordIfNewPasswordIsSameAsCurrent() {

        String currentPassword = "password";
        String newPassword = "password";
        String encodedCurrentPassword = "encodedPassword";

        User user = new User();
        user.setPassword(encodedCurrentPassword);

        UsernamePasswordAuthenticationToken connectedUser = mock(UsernamePasswordAuthenticationToken.class);
        when(connectedUser.getPrincipal()).thenReturn(user);

        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);

        userService.changePassword(connectedUser, currentPassword, newPassword);

        // When passwords are identical, UserService intentionally does nothing
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }
}
