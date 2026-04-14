package com.app.event.dto;

import com.app.category.entity.CategoryContent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryContentResponseDto {

    private UUID id;

    private UUID categoryId;

    private CategoryContent.ContentType contentType;

    private String title;

    private String description;

    private List<Map<String, Object>> options;

    private Integer points;

    private Integer timeLimit;

    private CategoryContent.Difficulty difficulty;

    private Integer order;

    private Integer questionIndex;

    private Integer totalQuestions;
}
