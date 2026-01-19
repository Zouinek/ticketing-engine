package com.ticketmaster.ticketing_engine.service;

import com.ticketmaster.ticketing_engine.entity.User;
import com.ticketmaster.ticketing_engine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;

/**
 * <h1>User Management Service</h1>
 * <p>
 * Handles operations related to the user's profile and settings <i>after</i> they have logged in.
 * While {@code AuthenticationService} handles "getting in", this service handles
 * "staying managed".
 * </p>
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Changes the currently logged-in user's password.
     *
     * @param connectedUser The principal (current user) automatically injected by Spring Security.
     * @param currentPassword The password the user is currently using.
     * @param newPassword The new password they want to set.
     * @throws IllegalStateException If the passwords don't match or the user isn't found.
     */
    public void changePassword(Principal connectedUser, String currentPassword,  String newPassword) {

        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalStateException("Wrong password");
        }

        if (!newPassword.equals(currentPassword)) {

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        }
    }

}
