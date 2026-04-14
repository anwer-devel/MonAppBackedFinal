package com.app.category.entity;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "category_contents", indexes = {
        @Index(name = "idx_content_category", columnList = "category_id"),
        @Index(name = "idx_content_type", columnList = "content_type"),
        @Index(name = "idx_content_order", columnList = "category_id, content_order"),
        @Index(name = "idx_content_active", columnList = "is_active")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryContent extends BaseEntity {

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    private ContentType contentType;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> options;

    @Builder.Default
    @Column(nullable = false)
    private Integer points = 10;

    @Column(name = "time_limit")
    private Integer timeLimit; // in seconds

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Difficulty difficulty = Difficulty.MEDIUM;

    @Column(name = "content_order", nullable = false)
    @Builder.Default
    private Integer order = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    // ===== Enums =====

    public enum ContentType {
        QUIZ, QUESTION, SPINNER
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}

