package com.app.auth.dto;

import com.app.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String email;
    private User.UserRole role;
    private Boolean emailVerified;

    // Profile fields
    private String username;
    private String avatarUrl;
    private String title;
    private Integer level;
    private Integer xp;
    private Integer nextLevelXp;
    private Integer streak;
    private Integer rank;
    private Integer eventsPlayed;
    private Integer teamPoints;
    private Integer couponsCount;
    private Boolean isOnline;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .emailVerified(user.getEmailVerified())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .title(user.getTitle())
                .level(user.getLevel())
                .xp(user.getXp())
                .nextLevelXp(user.getNextLevelXp())
                .streak(user.getStreak())
                .rank(user.getRank())
                .eventsPlayed(user.getEventsPlayed())
                .teamPoints(user.getTeamPoints())
                .couponsCount(user.getCouponsCount())
                .isOnline(user.getIsOnline())
                .build();
    }
}
