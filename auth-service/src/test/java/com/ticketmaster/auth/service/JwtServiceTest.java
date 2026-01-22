package com.ticketmaster.auth.service;


import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    @Mock
    private UserDetails userDetails;

    // JwtService expects BASE64, not hex.
    // This is 32 bytes (256-bit) base64 encoded.
    private static final String TEST_SECRET_BASE64 = "MDEyMzQ1Njc4OUFCQ0RFRjAxMjM0NTY3ODlBQkNERUY=";
    private static final long TEST_EXPIRATION_TIME = 86_400_000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET_BASE64);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION_TIME);
    }

    @Test
    void shouldGenerateValidToken() {
        when(userDetails.getUsername()).thenReturn("test@example.com");

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertEquals("test@example.com", jwtService.extractUsername(token));
    }

    @Test
    void shouldValidateCorrectToken() {
        when(userDetails.getUsername()).thenReturn("test@example.com");
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void shouldExtractUsernameFromToken() {
        when(userDetails.getUsername()).thenReturn("TEST");

        Map<String, Object> claims = new HashMap<>();
        claims.put("name", "TEST");

        String token = jwtService.generateToken(claims, userDetails);

        // Username is stored in the JWT subject, not in the "name" claim.
        assertEquals("TEST", jwtService.extractUsername(token));

        // If we want to assert a custom claim, we should extract it explicitly.
        String nameClaim = jwtService.extractClaim(token, (Claims c) -> c.get("name", String.class));
        assertEquals("TEST", nameClaim);
    }
    @Test
    void shouldExtractCustomClaims(){
        when(userDetails.getUsername()).thenReturn("admin@example.com");

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");
        claims.put("department", "IT");

        String token = jwtService.generateToken(claims, userDetails);

        String roleClaim = jwtService.extractClaim(token, (Claims c) -> c.get("role", String.class));
        String deptClaim = jwtService.extractClaim(token, (Claims c) -> c.get("department", String.class));

        assertEquals("ADMIN", roleClaim);
        assertEquals("IT", deptClaim);

    }
}
