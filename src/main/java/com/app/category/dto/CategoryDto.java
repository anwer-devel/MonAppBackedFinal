package com.app.category.dto;

import com.app.category.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for category response")
public class CategoryDto {

    @Schema(description = "Category ID")
    private UUID id;

    @Schema(description = "Category name")
    private String name;

    @Schema(description = "Category description")
    private String description;

    @Schema(description = "Cover image URL")
    private String coverImage;

    @Schema(description = "Category type")
    private Category.CategoryType type;

    @Schema(description = "Category status")
    private Category.CategoryStatus status;

    @Schema(description = "Category visibility")
    private Category.CategoryVisibility visibility;

    @Schema(description = "Created by role")
    private Category.CreatedByRole createdBy;

    @Schema(description = "Partner ID (null if created by admin)")
    private UUID partnerId;

    @Schema(description = "Category tags")
    private List<String> tags;

    @Schema(description = "Metadata")
    private Map<String, Object> metadata;

    @Schema(description = "Is active")
    private Boolean isActive;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;

    @Schema(description = "Updated at")
    private LocalDateTime updatedAt;
}

