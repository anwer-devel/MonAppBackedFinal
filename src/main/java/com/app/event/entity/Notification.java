package com.app.event.entity;

import com.app.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notif_user", columnList = "user_id"),
        @Index(name = "idx_notif_type", columnList = "type"),
        @Index(name = "idx_notif_read", columnList = "is_read"),
        @Index(name = "idx_notif_user_read", columnList = "user_id, is_read")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> data = new HashMap<>();

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    // ===== Enums =====

    public enum NotificationType {
        EVENT_STARTED,
        EVENT_REMINDER,
        EVENT_CANCELLED,
        FRIEND_REQUEST,
        FRIEND_ACCEPTED,
        SCORE_UPDATED,
        ACHIEVEMENT_UNLOCKED,
        SYSTEM
    }
}
