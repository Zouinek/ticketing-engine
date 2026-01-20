package com.ticketmaster.auth.controller;


import com.ticketmaster.auth.dto.request.AuthenticationRequest;
import com.ticketmaster.auth.dto.response.AuthenticationResponse;
import com.ticketmaster.auth.dto.request.RegisterRequest;
import com.ticketmaster.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * <h1>Authentication Controller</h1>
 * <p>
 * This controller handles the public-facing security endpoints.
 * Unlike other controllers in this API, these endpoints are <b>Whitelisted</b> in the
 * {@code SecurityConfig}, meaning anyone can access them without a token.
 * </p>
 * <h2>Responsibilities:</h2>
 * <ul>
 * <li><b>Register:</b> Creating new user accounts.</li>
 * <li><b>Authenticate:</b> Verifying credentials and issuing JWTs.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * Registers a new user in the system.
     * <p>
     * This method accepts user details (name, email, password), saves them to the database,
     * and immediately returns a valid JWT so the user is logged in right away.
     * </p>
     * <h3>Validation:</h3>
     * The {@code @Valid} annotation ensures that the request body meets all constraints
     * defined in the {@link RegisterRequest} DTO (e.g., valid email format, min password length).
     *
     * @param request The DTO containing the new user's registration data.
     * @return A {@link ResponseEntity} containing the JWT access token.
     */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthenticationResponse> registerUser(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    /**
     * Authenticates an existing user (Login).
     * <p>
     * This method checks the provided email and password against the database.
     * If they match, it generates and signs a new JWT for the user.
     * </p>
     *
     * @param request The DTO containing the user's login credentials.
     * @return A {@link ResponseEntity} containing the JWT access token.
     * @throws org.springframework.security.authentication.BadCredentialsException If the email/password is incorrect.
     */
    @PostMapping(value = "/authenticate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthenticationResponse> authenticateUser(
            @Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

}
