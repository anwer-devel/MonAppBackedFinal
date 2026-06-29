package com.erp.platform.core.security;

import com.erp.platform.core.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtConfig jwtConfig;

    public String generateAccessToken(UUID userId, String email, String role,
                                       UUID partnerId, String partnerCode,
                                       UUID defaultLocalId, List<UUID> localAccess) {
        Map<String, Object> claims = new HashMap<>();
        if (userId != null) claims.put("userId", userId.toString());
        claims.put("role", role);
        if (partnerId != null) claims.put("partnerId", partnerId.toString());
        if (partnerCode != null) claims.put("partnerCode", partnerCode);
        if (defaultLocalId != null) claims.put("defaultLocalId", defaultLocalId.toString());
        if (localAccess != null && !localAccess.isEmpty()) {
            claims.put("localAccess", localAccess.stream().map(UUID::toString).toList());
        }

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()
                        + jwtConfig.getAccessExpirySeconds() * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public String extractPartnerId(String token) {
        return extractAllClaims(token).get("partnerId", String.class);
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }

    public String extractPartnerCode(String token) {
        return extractAllClaims(token).get("partnerCode", String.class);
    }

    public String extractDefaultLocalId(String token) {
        return extractAllClaims(token).get("defaultLocalId", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractLocalAccess(String token) {
        return extractAllClaims(token).get("localAccess", List.class);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public long getRemainingExpiryMillis(String token) {
        Date expiration = extractExpiration(token);
        return expiration.getTime() - System.currentTimeMillis();
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // Pad short keys for development — production should use 256-bit+
            keyBytes = Arrays.copyOf(keyBytes, 32);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
