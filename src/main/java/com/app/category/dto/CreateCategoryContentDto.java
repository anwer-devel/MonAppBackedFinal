package com.app.category.dto;

import com.app.category.entity.CategoryContent;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "DTO for creating category content")
public class CreateCategoryContentDto {

    @NotNull(message = "Content type is required")
    @Schema(description = "Content type", example = "QUIZ")
    private CategoryContent.ContentType contentType;

    @NotBlank(message = "Title is required")
    @Schema(description = "Content title", example = "What is the capital of France?")
    private String title;

    @Schema(description = "Content description")
    private String description;

    @Schema(description = "Correct answer")
    private String correctAnswer;

    @Schema(description = "Answer options")
    private List<Map<String, Object>> options;

    @Builder.Default
    @Min(1)
    @Schema(description = "Points for correct answer", example = "10")
    private Integer points = 10;

    @Schema(description = "Time limit in seconds")
    private Integer timeLimit;

    @Schema(description = "Difficulty level", example = "MEDIUM")
    @Builder.Default
    private CategoryContent.Difficulty difficulty = CategoryContent.Difficulty.MEDIUM;

    @Builder.Default
    @Min(0)
    @Schema(description = "Order in the category", example = "0")
    private Integer order = 0;

    @Schema(description = "Metadata (spinner segments, probabilities, etc.)")
    private Map<String, Object> metadata;
}

