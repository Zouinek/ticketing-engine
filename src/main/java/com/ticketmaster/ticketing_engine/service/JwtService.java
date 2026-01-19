package com.ticketmaster.ticketing_engine.service;

import com.ticketmaster.ticketing_engine.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * <h1>JWT Service</h1>
 * <p>
 * This class handles all cryptographic operations related to JSON Web Tokens (JWT).
 * It is responsible for <b>Signing</b> (creation), <b>Parsing</b> (reading), and <b>Validating</b> tokens.
 * </p>
 * <h2>Key Responsibilities:</h2>
 * <ul>
 * <li>Generating a signed token when a user logs in.</li>
 * <li>Extracting the "Subject" (User Email) from a token.</li>
 * <li>Checking if a token has expired.</li>
 * </ul>
 */
@Service
public class JwtService {

    /**
     * The cryptographic key used to sign the tokens.
     * <p>
     * <b>Security Note:</b> This value is injected from {@code application.properties}.
     * It must be a strong, 256-bit Hex string to work with the HS256 algorithm.
     * </p>
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * The validity duration of the token in milliseconds.
     * (e.g., 86400000 ms = 24 hours).
     */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Extracts the Username (Email) from the token.
     *
     * @param token The JWT string.
     * @return The username (subject) stored in the token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Generic method to extract a specific piece of information (Claim) from the token.
     * <p>
     * This uses Java's {@link Function} interface to decouple the extraction logic.
     * Example usage: {@code extractClaim(token, Claims::getExpiration)}
     * </p>
     *
     * @param token          The JWT string.
     * @param claimsResolver The function that points to the desired claim.
     * @param <T>            The type of data being returned (String, Date, etc.).
     * @return The extracted claim value.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaimsJWT(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates a token for a user without any extra custom claims.
     *
     * @param userDetails The user to generate the token for.
     * @return A signed JWT string.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * The core method that builds the JWT.
     * <p>
     * <b>Structure of the Token:</b>
     * <ol>
     * <li><b>Claims:</b> Custom data (empty map by default).</li>
     * <li><b>Subject:</b> The unique identifier (User Email).</li>
     * <li><b>IssuedAt:</b> Current timestamp.</li>
     * <li><b>Expiration:</b> Current timestamp + configured duration.</li>
     * <li><b>Signature:</b> Encrypted hash of the header and payload using HS256.</li>
     * </ol>
     * </p>
     *
     * @param claims      Extra data to add to the payload (e.g., user role).
     * @param userDetails The authenticated user.
     * @return The final signed JWT string.
     */
    public String generateToken(
            Map<String, Object> claims,
            UserDetails userDetails) {
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInkey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates if a token belongs to the given user and is not expired.
     *
     * @param token       The JWT string.
     * @param userDetails The user trying to authenticate.
     * @return {@code true} if the token is valid; {@code false} otherwise.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Checks if the token's expiration date has passed.
     *
     * @param token The JWT string.
     * @return {@code true} if expired.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parses the token to read all data (Claims) inside it.
     * <p>
     * <b>Critical Security Check:</b> This method validates the signature!
     * If someone tampered with the token, {@code parseClaimsJws} will throw an exception
     * because the signature won't match the payload.
     * </p>
     *
     * @param token The JWT string.
     * @return The body (payload) of the token.
     */
    private Claims extractAllClaimsJWT(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInkey())
                .build()
                .parseClaimsJws(token)
                .getBody();

    }

    /**
     * Decodes the Secret Key from Base64 into a cryptographic Key object.
     *
     * @return A standard Key object usable by the HMAC-SHA algorithm.
     */
    private Key getSignInkey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
