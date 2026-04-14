package com.app.auth.entity;

import com.app.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email", unique = true),
        @Index(name = "idx_is_active", columnList = "is_active"),
        @Index(name = "idx_username", columnList = "username")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Builder.Default
    @Column(nullable = false)
    private Boolean emailVerified = false;

    // ===== Profile fields (Flutter spec) =====

    @Column(length = 50)
    private String username;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Builder.Default
    @Column(length = 100)
    private String title = "Débutant";

    @Builder.Default
    @Column(nullable = false)
    private Integer level = 1;

    @Builder.Default
    @Column(nullable = false)
    private Integer xp = 0;

    @Builder.Default
    @Column(name = "next_level_xp", nullable = false)
    private Integer nextLevelXp = 100;

    @Builder.Default
    @Column(nullable = false)
    private Integer streak = 0;

    @Builder.Default
    @Column(name = "user_rank", nullable = false)
    private Integer rank = 0;

    @Builder.Default
    @Column(name = "events_played", nullable = false)
    private Integer eventsPlayed = 0;

    @Builder.Default
    @Column(name = "team_points", nullable = false)
    private Integer teamPoints = 0;


    @Builder.Default
    @Column(name = "coupons_count", nullable = false)
    private Integer couponsCount = 0;

    @Builder.Default
    @Column(name = "is_online", nullable = false)
    private Boolean isOnline = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    public enum UserRole {
        ADMIN,
        PARTNER_OWNER,
        USER
    }
}

