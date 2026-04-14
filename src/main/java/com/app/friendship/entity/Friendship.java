package com.app.friendship.entity;

import com.app.auth.entity.User;
import com.app.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "friendships", uniqueConstraints = {
        @UniqueConstraint(name = "uk_friendship_pair", columnNames = {"user_id", "friend_id"})
}, indexes = {
        @Index(name = "idx_friendship_user", columnList = "user_id"),
        @Index(name = "idx_friendship_friend", columnList = "friend_id"),
        @Index(name = "idx_friendship_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friendship extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FriendshipStatus status = FriendshipStatus.PENDING;

    public enum FriendshipStatus {
        PENDING,
        ACCEPTED,
        BLOCKED
    }
}
