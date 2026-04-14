package com.app.category.mapper;

import com.app.category.dto.CategoryContentDto;
import com.app.category.dto.CreateCategoryContentDto;
import com.app.category.dto.UpdateCategoryContentDto;
import com.app.category.entity.CategoryContent;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class CategoryContentMapper {

    public CategoryContent toEntity(CreateCategoryContentDto dto) {
        if (dto == null) {
            return null;
        }

        return CategoryContent.builder()
                .contentType(dto.getContentType())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .correctAnswer(dto.getCorrectAnswer())
                .options(dto.getOptions())
                .points(dto.getPoints() != null ? dto.getPoints() : 10)
                .timeLimit(dto.getTimeLimit())
                .difficulty(dto.getDifficulty() != null ? dto.getDifficulty() : CategoryContent.Difficulty.MEDIUM)
                .order(dto.getOrder() != null ? dto.getOrder() : 0)
                .metadata(dto.getMetadata() != null ? dto.getMetadata() : new HashMap<>())
                .build();
    }

    public void updateEntityFromDto(UpdateCategoryContentDto dto, CategoryContent entity) {
        if (dto == null) {
            return;
        }

        if (dto.getContentType() != null) {
            entity.setContentType(dto.getContentType());
        }
        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getCorrectAnswer() != null) {
            entity.setCorrectAnswer(dto.getCorrectAnswer());
        }
        if (dto.getOptions() != null) {
            entity.setOptions(dto.getOptions());
        }
        if (dto.getPoints() != null) {
            entity.setPoints(dto.getPoints());
        }
        if (dto.getTimeLimit() != null) {
            entity.setTimeLimit(dto.getTimeLimit());
        }
        if (dto.getDifficulty() != null) {
            entity.setDifficulty(dto.getDifficulty());
        }
        if (dto.getOrder() != null) {
            entity.setOrder(dto.getOrder());
        }
        if (dto.getMetadata() != null) {
            entity.setMetadata(dto.getMetadata());
        }
    }

    public CategoryContentDto toDto(CategoryContent entity) {
        if (entity == null) {
            return null;
        }

        return CategoryContentDto.builder()
                .id(entity.getId())
                .categoryId(entity.getCategoryId())
                .contentType(entity.getContentType())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .correctAnswer(entity.getCorrectAnswer())
                .options(entity.getOptions())
                .points(entity.getPoints())
                .timeLimit(entity.getTimeLimit())
                .difficulty(entity.getDifficulty())
                .order(entity.getOrder())
                .metadata(entity.getMetadata())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

