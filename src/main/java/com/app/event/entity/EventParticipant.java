package com.app.event.entity;

import com.app.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_participants", indexes = {
        @Index(name = "idx_participant_event", columnList = "event_id"),
        @Index(name = "idx_participant_user", columnList = "user_id"),
        @Index(name = "idx_participant_event_user", columnList = "event_id, user_id", unique = true),
        @Index(name = "idx_participant_score", columnList = "event_id, score DESC"),
        @Index(name = "idx_participant_status", columnList = "status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_event_user", columnNames = {"event_id", "user_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventParticipant extends BaseEntity {

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ParticipantStatus status = ParticipantStatus.WAITING;

    @Column(name = "joined_at", nullable = false)
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "current_question_index", nullable = false)
    @Builder.Default
    private Integer currentQuestionIndex = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer score = 0;

    @Column(name = "correct_answers", nullable = false)
    @Builder.Default
    private Integer correctAnswers = 0;

    @Column(name = "wrong_answers", nullable = false)
    @Builder.Default
    private Integer wrongAnswers = 0;

    @Column
    private Integer rank;

    @Column(name = "is_online", nullable = false)
    @Builder.Default
    private Boolean isOnline = true;

    // ===== Enums =====

    public enum ParticipantStatus {
        WAITING, ACTIVE, COMPLETED, ABANDONED
    }
}
