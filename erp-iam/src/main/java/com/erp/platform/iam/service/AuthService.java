package com.erp.platform.iam.service;

import com.erp.platform.core.audit.AuditService;
import com.erp.platform.core.config.JwtConfig;
import com.erp.platform.core.exception.ResourceNotFoundException;
import com.erp.platform.core.exception.TokenExpiredException;
import com.erp.platform.core.security.JwtService;
import com.erp.platform.iam.dto.auth.*;
import com.erp.platform.iam.entity.Collaborator;
import com.erp.platform.iam.enums.CollaboratorStatus;
import com.erp.platform.iam.repository.CollaboratorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final CollaboratorRepository collaboratorRepository;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AuditService auditService;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    @Transactional
    public LoginResponse login(LoginRequest req, String ipAddress) {
        Collaborator collab = collaboratorRepository.findByEmailWithRelations(req.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(req.getPassword(), collab.getPasswordHash())) {
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }

        if (collab.getStatus() != CollaboratorStatus.ACTIVE) {
            throw new LockedException("Compte suspendu ou inactif");
        }

        String accessToken = jwtService.generateAccessToken(
                collab.getId(),
                collab.getEmail(),
                collab.getRole().name(),
                collab.getPartner() != null ? collab.getPartner().getId() : null,
                collab.getPartner() != null ? collab.getPartner().getCode() : null,
                collab.getDefaultLocal() != null ? collab.getDefaultLocal().getId() : null,
                collab.getLocalAccess()
        );

        String refreshToken = jwtService.generateRefreshToken();
        String refreshHash = jwtService.hashToken(refreshToken);

        collab.setRefreshTokenHash(refreshHash);
        collab.setRefreshTokenExpiry(
                LocalDateTime.now().plusSeconds(jwtConfig.getRefreshExpirySeconds()));
        collab.setLastLoginAt(LocalDateTime.now());
        collab.setLastLoginIp(ipAddress);
        collaboratorRepository.save(collab);

        auditService.log(collab.getId(), collab.getEmail(), "USER_LOGIN",
                "Collaborator", collab.getId(), null, null,
                collab.getPartner() != null ? collab.getPartner().getCode() : null,
                ipAddress);

        return buildLoginResponse(collab, accessToken, refreshToken);
    }

    @Transactional
    public LoginResponse refresh(RefreshRequest req) {
        String hash = jwtService.hashToken(req.getRefreshToken());

        Collaborator collab = collaboratorRepository.findByRefreshTokenHashWithRelations(hash)
                .orElseThrow(() -> new TokenExpiredException("Refresh token invalide"));

        if (collab.getRefreshTokenExpiry() == null
                || collab.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            collab.setRefreshTokenHash(null);
            collab.setRefreshTokenExpiry(null);
            collaboratorRepository.save(collab);
            throw new TokenExpiredException("Refresh token expiré");
        }

        String newAccessToken = jwtService.generateAccessToken(
                collab.getId(),
                collab.getEmail(),
                collab.getRole().name(),
                collab.getPartner() != null ? collab.getPartner().getId() : null,
                collab.getPartner() != null ? collab.getPartner().getCode() : null,
                collab.getDefaultLocal() != null ? collab.getDefaultLocal().getId() : null,
                collab.getLocalAccess()
        );

        String newRefreshToken = jwtService.generateRefreshToken();
        String newRefreshHash = jwtService.hashToken(newRefreshToken);

        collab.setRefreshTokenHash(newRefreshHash);
        collab.setRefreshTokenExpiry(
                LocalDateTime.now().plusSeconds(jwtConfig.getRefreshExpirySeconds()));
        collaboratorRepository.save(collab);

        return buildLoginResponse(collab, newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken, String accessToken, UUID userId) {
        if (refreshToken != null) {
            String hash = jwtService.hashToken(refreshToken);
            collaboratorRepository.findByRefreshTokenHashWithRelations(hash)
                    .ifPresent(collab -> {
                        collab.setRefreshTokenHash(null);
                        collab.setRefreshTokenExpiry(null);
                        collaboratorRepository.save(collab);
                    });
        }

        if (accessToken != null) {
            try {
                long remainingMillis = jwtService.getRemainingExpiryMillis(accessToken);
                if (remainingMillis > 0) {
                    redisTemplate.opsForValue().set(
                            BLACKLIST_PREFIX + accessToken,
                            "blacklisted",
                            remainingMillis,
                            TimeUnit.MILLISECONDS);
                }
            } catch (Exception e) {
                log.debug("Could not blacklist access token: {}", e.getMessage());
            }
        }

        if (userId != null) {
            collaboratorRepository.findByIdAndIsDeletedFalse(userId)
                    .ifPresent(collab ->
                            auditService.log(userId, collab.getEmail(), "USER_LOGOUT",
                                    "Collaborator", userId, null, null,
                                    collab.getPartner() != null
                                            ? collab.getPartner().getCode() : null,
                                    null));
        }
    }

    public UserInfo getMe(UUID userId) {
        Collaborator collab = collaboratorRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator", "id", userId));
        return buildUserInfo(collab);
    }

    private LoginResponse buildLoginResponse(Collaborator collab,
                                              String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getAccessExpirySeconds())
                .user(buildUserInfo(collab))
                .build();
    }

    private UserInfo buildUserInfo(Collaborator collab) {
        UserInfo.UserInfoBuilder builder = UserInfo.builder()
                .id(collab.getId())
                .email(collab.getEmail())
                .firstName(collab.getFirstName())
                .lastName(collab.getLastName())
                .role(collab.getRole().name())
                .localAccess(collab.getLocalAccess());

        if (collab.getPartner() != null) {
            builder.partnerId(collab.getPartner().getId())
                    .partnerCode(collab.getPartner().getCode())
                    .partnerName(collab.getPartner().getName())
                    .partnerConfig(collab.getPartner().getConfig());
        }

        if (collab.getDefaultLocal() != null) {
            builder.defaultLocalId(collab.getDefaultLocal().getId())
                    .defaultLocalCode(collab.getDefaultLocal().getCode())
                    .defaultLocalName(collab.getDefaultLocal().getName());
        }

        return builder.build();
    }
}
