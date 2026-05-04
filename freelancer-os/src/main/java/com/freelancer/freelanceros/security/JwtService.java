package com.freelancer.freelanceros.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final Key SECRET_KEY =
            Keys.hmacShaKeyFor("freelancerosfreelancerosfreelancerosfreelanceros".getBytes());

    // ⏱️ Expiry
    private static final long ACCESS_EXPIRATION = 1000 * 60 * 15; // 15 min
    private static final long REFRESH_EXPIRATION = 1000L * 60 * 60 * 24 * 7; // 7 days

    // ─────────────────────────────────────────────

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ─────────────────────────────────────────────

    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails, ACCESS_EXPIRATION, "ACCESS");
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, REFRESH_EXPIRATION, "REFRESH");
    }

    private String generateToken(UserDetails userDetails, long expiration, String type) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", type);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
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
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}