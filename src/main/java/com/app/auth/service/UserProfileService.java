package com.app.auth.service;

import com.app.auth.dto.UpdateProfileRequest;
import com.app.auth.dto.UserDto;

import java.util.UUID;

public interface UserProfileService {
    UserDto getProfile(UUID userId);
    UserDto getPublicProfile(UUID userId);
    UserDto updateProfile(UUID userId, UpdateProfileRequest request);
    void addXp(UUID userId, int amount);
    void incrementEventsPlayed(UUID userId);
    void updateStreak(UUID userId);
    void setOnlineStatus(UUID userId, boolean online);
}
