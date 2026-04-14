package com.app.category.mapper;

import com.app.category.dto.CategoryDto;
import com.app.category.dto.CreateCategoryDto;
import com.app.category.dto.UpdateCategoryDto;
import com.app.category.entity.Category;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class CategoryMapper {

    public Category toEntity(CreateCategoryDto dto) {
        if (dto == null) {
            return null;
        }

        return Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .coverImage(dto.getCoverImage())
                .type(dto.getType())
                .visibility(dto.getVisibility())
                .tags(dto.getTags())
                .metadata(dto.getMetadata() != null ? dto.getMetadata() : new HashMap<>())
                .build();
    }

    public void updateEntityFromDto(UpdateCategoryDto dto, Category entity) {
        if (dto == null) {
            return;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getCoverImage() != null) {
            entity.setCoverImage(dto.getCoverImage());
        }
        if (dto.getType() != null) {
            entity.setType(dto.getType());
        }
        if (dto.getVisibility() != null) {
            entity.setVisibility(dto.getVisibility());
        }
        if (dto.getTags() != null) {
            entity.setTags(dto.getTags());
        }
        if (dto.getMetadata() != null) {
            entity.setMetadata(dto.getMetadata());
        }
    }

    public CategoryDto toDto(Category entity) {
        if (entity == null) {
            return null;
        }

        return CategoryDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .coverImage(entity.getCoverImage())
                .type(entity.getType())
                .status(entity.getStatus())
                .visibility(entity.getVisibility())
                .createdBy(entity.getCreatedBy())
                .partnerId(entity.getPartnerId())
                .tags(entity.getTags())
                .metadata(entity.getMetadata())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

