package com.app.auth.service.impl;

import com.app.auth.dto.UpdateProfileRequest;
import com.app.auth.dto.UserDto;
import com.app.auth.entity.User;
import com.app.auth.repository.UserRepository;
import com.app.auth.service.UserProfileService;
import com.app.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;

    // XP thresholds per level (index = level)
    private static final int[] LEVEL_THRESHOLDS = {
            0, 100, 250, 500, 800, 1200, 1800, 2500, 3500, 5000,
            7000, 9500, 12500, 16000, 20000, 25000, 31000, 38000, 46000, 55000
    };

    @Override
    @Transactional(readOnly = true)
    public UserDto getProfile(UUID userId) {
        User user = findUserOrThrow(userId);
        return UserDto.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getPublicProfile(UUID userId) {
        User user = findUserOrThrow(userId);
        // Return public info only (no email)
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .title(user.getTitle())
                .level(user.getLevel())
                .xp(user.getXp())
                .streak(user.getStreak())
                .rank(user.getRank())
                .eventsPlayed(user.getEventsPlayed())
                .isOnline(user.getIsOnline())
                .build();
    }

    @Override
    @Transactional
    public UserDto updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = findUserOrThrow(userId);

        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getTitle() != null) {
            user.setTitle(request.getTitle());
        }

        user = userRepository.save(user);
        log.info("User profile updated: {}", userId);
        return UserDto.fromEntity(user);
    }

    @Override
    @Transactional
    public void addXp(UUID userId, int amount) {
        User user = findUserOrThrow(userId);
        int newXp = user.getXp() + amount;
        user.setXp(newXp);

        // Level up check
        int newLevel = calculateLevel(newXp);
        if (newLevel > user.getLevel()) {
            user.setLevel(newLevel);
            user.setTitle(getTitleForLevel(newLevel));
            log.info("User {} leveled up to {}", userId, newLevel);
        }
        user.setNextLevelXp(getXpForNextLevel(newLevel));

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void incrementEventsPlayed(UUID userId) {
        User user = findUserOrThrow(userId);
        user.setEventsPlayed(user.getEventsPlayed() + 1);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateStreak(UUID userId) {
        User user = findUserOrThrow(userId);
        LocalDateTime lastLogin = user.getLastLoginAt();
        LocalDateTime now = LocalDateTime.now();

        if (lastLogin == null || lastLogin.toLocalDate().plusDays(1).equals(now.toLocalDate())) {
            user.setStreak(user.getStreak() + 1);
        } else if (!lastLogin.toLocalDate().equals(now.toLocalDate())) {
            user.setStreak(1); // Reset streak
        }

        user.setLastLoginAt(now);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void setOnlineStatus(UUID userId, boolean online) {
        User user = findUserOrThrow(userId);
        user.setIsOnline(online);
        userRepository.save(user);
    }

    // === Helpers ===

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private int calculateLevel(int xp) {
        for (int i = LEVEL_THRESHOLDS.length - 1; i >= 0; i--) {
            if (xp >= LEVEL_THRESHOLDS[i]) {
                return i + 1;
            }
        }
        return 1;
    }

    private int getXpForNextLevel(int currentLevel) {
        if (currentLevel < LEVEL_THRESHOLDS.length) {
            return LEVEL_THRESHOLDS[currentLevel];
        }
        return LEVEL_THRESHOLDS[LEVEL_THRESHOLDS.length - 1] + (currentLevel - LEVEL_THRESHOLDS.length + 1) * 10000;
    }

    private String getTitleForLevel(int level) {
        if (level <= 3) return "Débutant";
        if (level <= 6) return "Aventurier";
        if (level <= 10) return "Expert";
        if (level <= 15) return "Maître";
        return "Légende";
    }
}
