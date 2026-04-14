package com.app.category.dto;

import com.app.category.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for updating a category")
public class UpdateCategoryDto {

    @Schema(description = "Category name")
    private String name;

    @Schema(description = "Category description")
    private String description;

    @Schema(description = "Cover image URL")
    private String coverImage;

    @Schema(description = "Category type")
    private Category.CategoryType type;

    @Schema(description = "Category visibility")
    private Category.CategoryVisibility visibility;

    @Schema(description = "Category tags")
    private List<String> tags;

    @Schema(description = "Metadata")
    private Map<String, Object> metadata;
}

