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
@Table(name = "event_answers", indexes = {
        @Index(name = "idx_answer_event", columnList = "event_id"),
        @Index(name = "idx_answer_participant", columnList = "participant_id"),
        @Index(name = "idx_answer_content", columnList = "content_id"),
        @Index(name = "idx_answer_event_user", columnList = "event_id, user_id"),
        @Index(name = "idx_answer_question", columnList = "participant_id, question_index")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_participant_content", columnNames = {"participant_id", "content_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventAnswer extends BaseEntity {

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "participant_id", nullable = false)
    private UUID participantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "content_id", nullable = false)
    private UUID contentId;

    @Column(name = "question_index", nullable = false)
    private Integer questionIndex;

    @Column(name = "selected_answer", columnDefinition = "TEXT")
    private String selectedAnswer;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "points_earned", nullable = false)
    @Builder.Default
    private Integer pointsEarned = 0;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "speed_bonus", nullable = false)
    @Builder.Default
    private Integer speedBonus = 0;

    @Column(name = "answered_at", nullable = false)
    @Builder.Default
    private LocalDateTime answeredAt = LocalDateTime.now();
}
