package com.freelancer.freelanceros.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-expiration:900000}")
    private long accessExpiration;

    @Value("${app.jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    // ─────────────────────────────────────────────

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ─────────────────────────────────────────────

    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails, accessExpiration, "ACCESS");
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, refreshExpiration, "REFRESH");
    }

    private String generateToken(UserDetails userDetails, long expiration, String type) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", type);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ─────────────────────────────────────────────

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, "ACCESS");
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, "REFRESH");
    }

    private boolean isTokenValid(String token, UserDetails userDetails, String expectedType) {

        final String username = extractUsername(token);
        final String tokenType = extractTokenType(token);

        return username.equals(userDetails.getUsername())
                && tokenType.equals(expectedType)
                && !isTokenExpired(token);
    }

    // ─────────────────────────────────────────────

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("tokenType", String.class));
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            throw new IllegalStateException(
                "JWT_SECRET environment variable is not set. " +
                "Generate a secret with: openssl rand -base64 32"
            );
        }
        byte[] keyBytes = jwtSecret.getBytes();
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                "JWT_SECRET must be at least 32 characters long. " +
                "Current length: " + keyBytes.length
            );
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}