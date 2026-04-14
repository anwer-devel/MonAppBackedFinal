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
@Table(name = "categories", indexes = {
        @Index(name = "idx_category_partner", columnList = "partner_id"),
        @Index(name = "idx_category_status", columnList = "status"),
        @Index(name = "idx_category_type", columnList = "type"),
        @Index(name = "idx_category_visibility", columnList = "visibility"),
        @Index(name = "idx_category_created_by", columnList = "created_by"),
        @Index(name = "idx_category_active", columnList = "is_active")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @NotBlank
    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoryType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoryStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoryVisibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "created_by", nullable = false, length = 20)
    private CreatedByRole createdBy;

    @Column(name = "partner_id")
    private UUID partnerId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "category_tags", joinColumns = @JoinColumn(name = "category_id"))
    @Column(name = "tag")
    private List<String> tags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    // ===== Enums =====

    public enum CategoryType {
        QUIZ, QUESTION, SPINNER, MIXED
    }

    public enum CategoryStatus {
        DRAFT, PENDING_APPROVAL, APPROVED, REJECTED
    }

    public enum CategoryVisibility {
        PUBLIC, PRIVATE
    }

    public enum CreatedByRole {
        ADMIN, PARTNER
    }
}

