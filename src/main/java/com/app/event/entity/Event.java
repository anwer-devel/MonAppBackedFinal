package com.app.event.entity;

import com.app.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_event_partner", columnList = "partner_id"),
        @Index(name = "idx_event_status", columnList = "status"),
        @Index(name = "idx_event_type", columnList = "event_type"),
        @Index(name = "idx_event_category", columnList = "category_id"),
        @Index(name = "idx_event_scheduled", columnList = "scheduled_at"),
        @Index(name = "idx_event_active", columnList = "is_active"),
        @Index(name = "idx_event_partner_status", columnList = "partner_id, status, scheduled_at")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event extends BaseEntity {

    @NotBlank
    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EventStatus status = EventStatus.DRAFT;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "partner_id")
    private UUID partnerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "created_by", nullable = false, length = 20)
    private CreatedByRole createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EventVisibility visibility = EventVisibility.PUBLIC;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "current_participants", nullable = false)
    @Builder.Default
    private Integer currentParticipants = 0;

    @Column(name = "current_question_index", nullable = false)
    @Builder.Default
    private Integer currentQuestionIndex = 0;

    @Column(name = "question_time_limit", nullable = false)
    @Builder.Default
    private Integer questionTimeLimit = 30;

    @Column(name = "total_questions", nullable = false)
    @Builder.Default
    private Integer totalQuestions = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    // ===== Enums =====

    public enum EventType {
        SIMPLE, LIVE
    }

    public enum EventStatus {
        DRAFT, SCHEDULED, WAITING_ROOM, LIVE, FINISHED, CANCELLED
    }

    public enum CreatedByRole {
        ADMIN, PARTNER
    }

    public enum EventVisibility {
        PUBLIC, PRIVATE
    }
}
