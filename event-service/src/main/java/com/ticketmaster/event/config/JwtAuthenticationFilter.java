package com.ticketmaster.event.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT Authentication Filter for Event-Service.
 * Validates JWT tokens issued by Auth-Service and sets authentication context.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Get Authorization header
        final String authHeader = request.getHeader("Authorization");

        // If no header or doesn't start with "Bearer ", continue without authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract JWT token (remove "Bearer " prefix)
            final String jwt = authHeader.substring(7);

            // Validate token
            if (jwtService.validateToken(jwt)) {
                // Extract username and role from token
                String username = jwtService.extractUsername(jwt);
                String role = jwtService.extractRole(jwt);

                logger.info("JWT Authentication - Username: " + username + ", Role: " + role);

                // Create authentication object with role
                // Note: Spring Security expects "ROLE_" prefix
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.singletonList(authority)
                );

                // Set authentication details
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authToken);

                logger.info("Authentication set successfully for user: " + username + " with role: ROLE_" + role);
            } else {
                logger.warn("JWT token validation failed - token is invalid or expired");
            }
        } catch (Exception e) {
            // Log error but don't fail the request - just don't authenticate
            logger.error("JWT validation failed: " + e.getMessage(), e);
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}
