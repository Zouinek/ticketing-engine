package com.ticketmaster.ticketing_engine.config;

import com.ticketmaster.ticketing_engine.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.lang.NonNull;

import java.io.IOException;

/**
 * <h1>JWT Authentication Filter</h1>
 * <p>
 * This filter intercepts every incoming HTTP request to check for a valid JWT (JSON Web Token).
 * It acts as the "Security Guard" at the door of the application.
 * </p>
 * * <h2>How it works:</h2>
 * <ol>
 * <li>Checks the "Authorization" header for a "Bearer" token.</li>
 * <li>Extracts the user email from the token.</li>
 * <li>Validates the token against the database and secret key.</li>
 * <li>If valid, it manually authenticates the user in Spring Security's context.</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final UserDetailsService userDetailsService;

    /**
     * The core logic that runs for every request.
     *
     * @param request     The incoming HTTP request.
     * @param response    The outgoing HTTP response.
     * @param filterChain The chain of other filters (so we can pass the request along).
     * @throws ServletException If a servlet error occurs.
     * @throws IOException      If an input/output error occurs.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        // Check for the Authorization Header
        final String authorizationHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        // If header is missing or doesn't start with "Bearer", ignore it and pass along.
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the Token (Remove "Bearer " prefix)
        jwt = authorizationHeader.substring(7);

        // Extract User Email from Token
        userEmail = jwtService.extractUsername(jwt);

        // Validate and Authenticate
        // We only authenticate if the user is not already authenticated
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load user details from the database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Check if token is valid (matches user and not expired)
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Create an Authentication Token required by Spring Security
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // Add extra request details (IP, Session ID, etc.)
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Tell Spring Security "This user is valid!"
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }

        }

        // Continue the filter chain (Pass to the next filter)
        filterChain.doFilter(request, response);

    }
}
