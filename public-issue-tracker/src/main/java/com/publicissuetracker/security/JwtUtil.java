package com.publicissuetracker.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * Simple JWT utility using jjwt (0.11.5).
 *
 * - Generates tokens with subject = userId and claims: email, role
 * - Validates tokens and extracts user id
 *
 * Note: keep the secret safe in production (env var / vault).
 */
@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration}") long expirationMs
    ) {
        // create a signing key from the configured secret
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Generate a JWT for a user.
     *
     * @param userId the user's id (subject)
     * @param email the user's email (stored as claim)
     * @param role the user's role (stored as claim)
     * @return compact JWT string
     */
    public String generateToken(String userId, String email, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(expirationMs);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .addClaims(Map.of(
                        "email", email,
                        "role", role
                ))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validate the token signature and expiration.
     * @param token the JWT string (may include "Bearer " prefix; this method does not strip it)
     * @return true if valid
     */
    public boolean validateToken(String token) {
        try {
            // parse will throw on invalid/expired token
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException ex) {
            // invalid signature / malformed
            return false;
        } catch (ExpiredJwtException ex) {
            // token expired
            return false;
        } catch (UnsupportedJwtException | IllegalArgumentException ex) {
            // other parse issues
            return false;
        }
    }

    /**
     * Extract user id (subject) from token.
     * @param token the compact JWT
     * @return subject (user id) or null if token invalid
     */
    public String getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException ex) {
            return null;
        }
    }

    /**
     * Extract a claim value by name (e.g. "email" or "role"), or null if not present/invalid.
     */
    public String getClaim(String token, String claimName) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Object v = claims.get(claimName);
            return v == null ? null : v.toString();
        } catch (JwtException ex) {
            return null;
        }
    }
}
