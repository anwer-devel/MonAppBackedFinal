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
@Table(name = "user_scores", indexes = {
        @Index(name = "idx_score_user", columnList = "user_id"),
        @Index(name = "idx_score_user_partner", columnList = "user_id, partner_id", unique = true),
        @Index(name = "idx_score_global", columnList = "total_score DESC"),
        @Index(name = "idx_score_partner_rank", columnList = "partner_id, total_score DESC")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_partner", columnNames = {"user_id", "partner_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserScore extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "partner_id")
    private UUID partnerId;

    @Column(name = "total_score", nullable = false)
    @Builder.Default
    private Long totalScore = 0L;

    @Column(name = "total_events", nullable = false)
    @Builder.Default
    private Integer totalEvents = 0;

    @Column(name = "total_correct_answers", nullable = false)
    @Builder.Default
    private Integer totalCorrectAnswers = 0;

    @Column(name = "total_wrong_answers", nullable = false)
    @Builder.Default
    private Integer totalWrongAnswers = 0;

    @Column
    private Integer rank;

    @Column(name = "last_updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime lastUpdatedAt = LocalDateTime.now();
}
