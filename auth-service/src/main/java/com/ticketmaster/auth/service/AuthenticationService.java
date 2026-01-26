package com.ticketmaster.auth.service;

import com.ticketmaster.auth.dto.request.AuthenticationRequest;
import com.ticketmaster.auth.dto.response.AuthenticationResponse;
import com.ticketmaster.auth.dto.request.RegisterRequest;
import com.ticketmaster.auth.entity.User;
import com.ticketmaster.auth.repository.UserRepository;
import com.ticketmaster.auth.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
// * <h1>Authentication Service</h1>
 * <p>
 * This service contains the core business logic for User Registration and Login.
 * It acts as the coordinator between the Database (User Repo), Security Tools (Password Encoder),
 * and the Token Generator (JwtService).
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {


    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user into the database.
     * <p>
     * <b>Process Flow:</b>
     * <ol>
     * <li>Converts the {@link RegisterRequest} DTO into a {@link User} entity.</li>
     * <li><b>Hashes the password</b> using BCrypt so we never store plain text passwords.</li>
     * <li>Assigns the default role ({@code Role.USER}).</li>
     * <li>Saves the user to the PostgreSQL database.</li>
     * <li>Immediately generates and returns a JWT so the user is logged in automatically.</li>
     * </ol>
     * </p>
     *
     * @param request The registration data (name, email, password) from the frontend.
     * @return An {@link AuthenticationResponse} containing the JWT access token.
     */
    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .build();
        userRepository.save(user);

        // Add role to JWT claims
        var claims = new java.util.HashMap<String, Object>();
        claims.put("role", user.getRole().name());
        var jwtToken = jwtService.generateToken(claims, user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    /**
     * Authenticates a user and issues a token (Login).
     * <p>
     * <b>Process Flow:</b>
     * <ol>
     * <li>Delegates credentials verification to the {@link AuthenticationManager}.</li>
     * <li>If the password is wrong, Spring throws a {@code BadCredentialsException} here.</li>
     * <li>If valid, we fetch the User entity from the database.</li>
     * <li>We generate a new JWT signed with our secret key.</li>
     * </ol>
     * </p>
     *
     * @param request The login credentials (email and password).
     * @return An {@link AuthenticationResponse} containing the valid JWT access token.
     * @throws org.springframework.security.core.AuthenticationException If login fails.
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        // Add role to JWT claims
        var claims = new java.util.HashMap<String, Object>();
        claims.put("role", user.getRole().name());
        var jwtToken = jwtService.generateToken(claims, user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
