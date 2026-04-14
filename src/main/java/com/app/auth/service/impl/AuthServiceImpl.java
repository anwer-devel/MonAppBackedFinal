package com.app.auth.service.impl;

import com.app.auth.dto.*;
import com.app.auth.entity.User;
import com.app.auth.repository.UserRepository;
import com.app.auth.service.AuthService;
import com.app.auth.service.JwtService;
import com.app.common.exception.BadRequestException;
import com.app.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        var user = userRepository.findByEmailAndIsActiveTrue(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        // Store refresh token in Redis
        storeRefreshToken(user.getId(), refreshToken);

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        user.setIsOnline(true);
        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserDto.fromEntity(user))
                .expiresIn(3600000) // 1 hour
                .build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("User already exists with this email");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.UserRole.USER);
        user.setEmailVerified(false);
        user.setIsActive(true);
        user.setUsername(request.getUsername() != null ? request.getUsername() : request.getEmail().split("@")[0]);
        user.setLastLoginAt(LocalDateTime.now());
        user.setIsOnline(true);

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        storeRefreshToken(user.getId(), refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserDto.fromEntity(user))
                .expiresIn(3600000) // 1 hour
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!jwtService.isTokenValid(request.getRefreshToken())) {
            throw new BadRequestException("Invalid refresh token");
        }

        UUID userId = jwtService.extractUserId(request.getRefreshToken());
        String storedToken = redisTemplate.opsForValue().get("refresh:" + userId);

        if (!request.getRefreshToken().equals(storedToken)) {
            throw new BadRequestException("Refresh token has been revoked");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getIsActive()) {
            throw new BadRequestException("User account is inactive");
        }

        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtService.generateRefreshToken(user.getId());

        storeRefreshToken(user.getId(), newRefreshToken);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(UserDto.fromEntity(user))
                .expiresIn(900000)
                .build();
    }

    @Override
    @Transactional
    public void logout(UUID userId) {
        redisTemplate.delete("refresh:" + userId);
        log.info("User logged out: {}", userId);
    }

    private void storeRefreshToken(UUID userId, String token) {
        redisTemplate.opsForValue().set("refresh:" + userId, token, 7, TimeUnit.DAYS);
    }
}

