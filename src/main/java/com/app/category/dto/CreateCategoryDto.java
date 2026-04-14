package com.app.category.dto;

import com.app.category.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for creating a category")
public class CreateCategoryDto {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
    @Schema(description = "Category name", example = "Quiz Sports")
    private String name;

    @Schema(description = "Category description", example = "Quiz questions about various sports")
    private String description;

    @Schema(description = "Cover image URL", example = "https://example.com/image.jpg")
    private String coverImage;

    @NotNull(message = "Category type is required")
    @Schema(description = "Category type", example = "QUIZ")
    private Category.CategoryType type;

    @NotNull(message = "Visibility is required")
    @Schema(description = "Category visibility", example = "PUBLIC")
    private Category.CategoryVisibility visibility;

    @Schema(description = "Category tags")
    private List<String> tags;

    @Schema(description = "Metadata (scoring config, rules, etc.)")
    private Map<String, Object> metadata;
}

