package com.ticketmaster.auth.config;


import com.ticketmaster.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldPassThrough_whenNoAuthorizationHeader() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verifyNoInteractions(jwtService, userDetailsService);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_shouldPassThrough_whenAuthorizationHeaderIsNotBearer() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc123");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verifyNoInteractions(jwtService, userDetailsService);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_shouldAuthenticate_whenTokenIsValid_andNoExistingAuthentication() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("Authorization", "Bearer jwt-token");
        var response = new MockHttpServletResponse();

        UserDetails userDetails = new User("user@email.com", "pw", Collections.emptyList());

        when(jwtService.extractUsername("jwt-token")).thenReturn("user@email.com");
        when(userDetailsService.loadUserByUsername("user@email.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("jwt-token", userDetails)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        verify(jwtService).extractUsername("jwt-token");
        verify(userDetailsService).loadUserByUsername("user@email.com");
        verify(jwtService).isTokenValid("jwt-token", userDetails);
        verify(filterChain).doFilter(request, response);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertInstanceOf(UsernamePasswordAuthenticationToken.class, authentication);
        assertEquals("user@email.com", authentication.getName());
        assertTrue(authentication.isAuthenticated());
        assertNotNull(authentication.getDetails());
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenTokenIsInvalid() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer jwt-token");
        var response = new MockHttpServletResponse();

        UserDetails userDetails = new User("user@email.com", "pw", Collections.emptyList());

        when(jwtService.extractUsername("jwt-token")).thenReturn("user@email.com");
        when(userDetailsService.loadUserByUsername("user@email.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("jwt-token", userDetails)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_shouldNotReAuthenticate_whenContextAlreadyHasAuthentication() throws ServletException, IOException {
        // Pre-authenticate
        var existing = new UsernamePasswordAuthenticationToken("already", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(existing);

        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer jwt-token");
        var response = new MockHttpServletResponse();

        // even if token exists, filter should skip because auth is already set
        when(jwtService.extractUsername("jwt-token")).thenReturn("user@email.com");

        filter.doFilter(request, response, filterChain);

        // It should not load user details nor validate token
        verify(jwtService).extractUsername("jwt-token");
        verifyNoInteractions(userDetailsService);
        verify(jwtService, never()).isTokenValid(any(), any());
        verify(filterChain).doFilter(request, response);

        assertSame(existing, SecurityContextHolder.getContext().getAuthentication());
    }
}
