package com.app.category.dto;

import com.app.category.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for filtering categories")
public class FilterCategoryDto {

    @Schema(description = "Filter by type")
    private Category.CategoryType type;

    @Schema(description = "Filter by visibility")
    private Category.CategoryVisibility visibility;

    @Schema(description = "Filter by status")
    private Category.CategoryStatus status;

    @Schema(description = "Filter by partner ID")
    private UUID partnerId;

    @Schema(description = "Filter by tags")
    private List<String> tags;

    @Schema(description = "Search in name and description")
    private String search;

    @Builder.Default
    @Schema(description = "Page number (0-based)", example = "0")
    private Integer page = 0;

    @Builder.Default
    @Schema(description = "Page size", example = "20")
    private Integer limit = 20;
}

