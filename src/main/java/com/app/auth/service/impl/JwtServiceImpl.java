package com.app.auth.service.impl;

import com.app.auth.entity.User;
import com.app.auth.repository.UserRepository;
import com.app.auth.service.JwtService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final UserRepository userRepository;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    @Override
    public String generateAccessToken(UUID userId, String email) {
        // Load user role from repository
        var userOpt = userRepository.findById(userId);
        // FIX: Spring Security hasRole() expects ROLE_ prefix
        String role = userOpt.map(u -> "ROLE_" + u.getRole().name()).orElse("ROLE_USER");
        String username = userOpt.map(u -> u.getUsername()).orElse(email);

        return Jwts.builder()
                .subject(userId.toString())  // sub = userId (UUID) for Angular
                .claim("userId", userId.toString())
                .claim("role", role)         // role = "ROLE_USER" | "ROLE_ADMIN" | "ROLE_PARTNER_OWNER"
                .claim("email", email)
                .claim("username", username)
                .claim("type", "ACCESS")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String generateRefreshToken(UUID userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "REFRESH")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.warn("Invalid token: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public UUID extractUserId(String token) {
        Claims claims = extractClaims(token);
        return UUID.fromString((String) claims.get("userId"));
    }

    @Override
    public String extractEmail(String token) {
        Claims claims = extractClaims(token);
        return (String) claims.get("email");
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}

