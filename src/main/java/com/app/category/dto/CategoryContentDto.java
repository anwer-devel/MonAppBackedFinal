package com.app.category.dto;

import com.app.category.entity.CategoryContent;
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
@Schema(description = "DTO for category content response")
public class CategoryContentDto {

    @Schema(description = "Content ID")
    private UUID id;

    @Schema(description = "Category ID")
    private UUID categoryId;

    @Schema(description = "Content type")
    private CategoryContent.ContentType contentType;

    @Schema(description = "Content title")
    private String title;

    @Schema(description = "Content description")
    private String description;

    @Schema(description = "Correct answer")
    private String correctAnswer;

    @Schema(description = "Answer options")
    private List<Map<String, Object>> options;

    @Schema(description = "Points for correct answer")
    private Integer points;

    @Schema(description = "Time limit in seconds")
    private Integer timeLimit;

    @Schema(description = "Difficulty level")
    private CategoryContent.Difficulty difficulty;

    @Schema(description = "Order in the category")
    private Integer order;

    @Schema(description = "Metadata")
    private Map<String, Object> metadata;

    @Schema(description = "Is active")
    private Boolean isActive;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;

    @Schema(description = "Updated at")
    private LocalDateTime updatedAt;
}

