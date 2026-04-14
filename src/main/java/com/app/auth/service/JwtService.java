package com.app.auth.service;

import io.jsonwebtoken.Claims;

import java.util.UUID;

public interface JwtService {
    String generateAccessToken(UUID userId, String email);
    String generateRefreshToken(UUID userId);
    Claims extractClaims(String token);
    UUID extractUserId(String token);
    String extractEmail(String token);
    boolean isTokenValid(String token);
    boolean isTokenExpired(String token);
}

