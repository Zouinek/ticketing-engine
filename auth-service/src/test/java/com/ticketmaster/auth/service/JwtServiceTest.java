package com.ticketmaster.auth.service;


import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    @Mock
    private UserDetails userDetails;

    // JwtService expects BASE64 (see Decoders.BASE64.decode(secretKey)).
    // This string decodes to ASCII: 0123456789ABCDEF0123456789ABCDEF (32 bytes).
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

    // valid Token valid User = Happy Path
    @Test
    void shouldValidateCorrectToken() {
        when(userDetails.getUsername()).thenReturn("test@example.com");
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
    }

    // valid Token wrong User
    @Test
    void shouldInvalidateIncorrectToken() {
        when(userDetails.getUsername()).thenReturn("test@example.com");
        String token = jwtService.generateToken(userDetails);

        UserDetails wrongUserDetails = org.mockito.Mockito.mock(UserDetails.class);
        when(wrongUserDetails.getUsername()).thenReturn("bad@example.com");

        boolean invalid = jwtService.isTokenValid(token, wrongUserDetails);

        assertFalse(invalid, "Token should be invalid for wrong user");
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
    void shouldExtractCustomClaims() {
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

    @Test
    void shouldExtractAllClaimsJWT() {
        when(userDetails.getUsername()).thenReturn("test@example.com");

        Map<String, Object> claims = new HashMap<>();
        claims.put("name", "USER");
        claims.put("lastname", "EXAMPLE");
        claims.put("email", "test@example.com");
        claims.put("role", "ADMIN");
        claims.put("department", "IT");

        String token = jwtService.generateToken(claims, userDetails);

        Claims allClaim = jwtService.extractAllClaimsJWT(token);

        assertEquals("USER", allClaim.get("name"));
        assertEquals("EXAMPLE", allClaim.get("lastname"));
        assertEquals("test@example.com", allClaim.get("email"));
        assertEquals("ADMIN", allClaim.get("role"));
        assertEquals("IT", allClaim.get("department"));

    }

    // expired Token
    @Test
    void shouldInvalidateExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1L);
        when(userDetails.getUsername()).thenReturn("expired@user.com");

        String token = jwtService.generateToken(userDetails);

        assertFalse(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void shouldInvalidateMalformedToken() {

        when(userDetails.getUsername()).thenReturn("test@example.com");
        String token = jwtService.generateToken(userDetails);
        String malformedToken = token.substring(0, token.length() - 10); // Remove last 10 chars to corrupt

        assertFalse(jwtService.isTokenValid(malformedToken, userDetails));
    }

    @Test
    void shouldInvalidateTokenWithInvalidSignature() {
        when(userDetails.getUsername()).thenReturn("sig@user.com");

        byte[] otherSecretBytes = new byte[32];
        new SecureRandom().nextBytes(otherSecretBytes);
        String otherSecretBase64 = Base64.getEncoder().encodeToString(otherSecretBytes);

        JwtService otherJwtService = new JwtService();
        ReflectionTestUtils.setField(otherJwtService, "secretKey", otherSecretBase64);
        ReflectionTestUtils.setField(otherJwtService, "jwtExpiration", TEST_EXPIRATION_TIME);

        String tokenSignedWithDifferentKey = otherJwtService.generateToken(userDetails);

        assertFalse(jwtService.isTokenValid(tokenSignedWithDifferentKey, userDetails));
    }

    @Test
    void shouldInvalidateEmptyToken() {
        assertFalse(jwtService.isTokenValid("", userDetails));
    }

    @Test
    void shouldInvalidateNullToken() {
        assertFalse(jwtService.isTokenValid(null, userDetails));
    }

}
