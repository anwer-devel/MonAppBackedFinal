package com.app.category.service.impl;

import com.app.category.dto.*;
import com.app.category.entity.Category;
import com.app.category.entity.CategoryContent;
import com.app.category.event.*;
import com.app.category.mapper.CategoryContentMapper;
import com.app.category.mapper.CategoryMapper;
import com.app.category.repository.CategoryContentRepository;
import com.app.category.repository.CategoryRepository;
import com.app.category.service.CategoryService;
import com.app.common.exception.BadRequestException;
import com.app.common.exception.ForbiddenException;
import com.app.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryContentRepository contentRepository;
    private final CategoryMapper categoryMapper;
    private final CategoryContentMapper contentMapper;
    private final CategoryEventPublisher eventPublisher;
    private final RedisTemplate<String, Object> redisTemplate;

    // ===== REDIS Cache Keys =====
    private static final String CACHE_CATEGORIES_BY_PARTNER = "categories:partner:";
    private static final String CACHE_CATEGORY_WITH_CONTENT = "category:";
    private static final String CACHE_SUFFIX_FULL = ":full";
    private static final long CACHE_TTL = 300; // 5 minutes

    @Override
    @Transactional
    public CategoryDto createPublicCategory(CreateCategoryDto dto) {
        log.info("Creating public category: {}", dto.getName());

        Category category = categoryMapper.toEntity(dto);
        category.setStatus(Category.CategoryStatus.APPROVED);
        category.setCreatedBy(Category.CreatedByRole.ADMIN);
        category.setPartnerId(null);

        Category savedCategory = categoryRepository.save(category);

        // Publish event
        eventPublisher.publishCategoryCreated(
                CategoryCreatedEvent.builder()
                        .categoryId(savedCategory.getId())
                        .partnerId(null)
                        .categoryType(savedCategory.getType().toString())
                        .categoryName(savedCategory.getName())
                        .timestamp(System.currentTimeMillis())
                        .build()
        );

        log.info("Public category created: {}", savedCategory.getId());
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDto approveCategory(UUID categoryId, ApproveCategoryDto dto) {
        log.info("Approving/Rejecting category: {} with status: {}", categoryId, dto.getStatus());

        Category category = getCategoryById(categoryId);

        // Verify it's in pending status
        if (category.getStatus() != Category.CategoryStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Category is not in pending approval status");
        }

        category.setStatus(dto.getStatus());

        // Store rejection reason in metadata if rejected
        if (dto.getStatus() == Category.CategoryStatus.REJECTED) {
            if (category.getMetadata() == null) {
                category.setMetadata(new HashMap<>());
            }
            category.getMetadata().put("rejectionReason", dto.getRejectionReason());
        }

        Category updatedCategory = categoryRepository.save(category);

        // Publish appropriate event
        if (dto.getStatus() == Category.CategoryStatus.APPROVED) {
            eventPublisher.publishCategoryApproved(
                    CategoryApprovedEvent.builder()
                            .categoryId(updatedCategory.getId())
                            .partnerId(updatedCategory.getPartnerId())
                            .categoryName(updatedCategory.getName())
                            .timestamp(System.currentTimeMillis())
                            .build()
            );
            invalidateCache(updatedCategory.getPartnerId());
        } else if (dto.getStatus() == Category.CategoryStatus.REJECTED) {
            eventPublisher.publishCategoryRejected(
                    CategoryRejectedEvent.builder()
                            .categoryId(updatedCategory.getId())
                            .partnerId(updatedCategory.getPartnerId())
                            .categoryName(updatedCategory.getName())
                            .rejectionReason(dto.getRejectionReason())
                            .timestamp(System.currentTimeMillis())
                            .build()
            );
        }

        log.info("Category {} updated to status: {}", categoryId, dto.getStatus());
        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDto> getAllCategories(FilterCategoryDto filterDto) {
        log.debug("Getting all categories with filters");

        Pageable pageable = PageRequest.of(filterDto.getPage(), filterDto.getLimit());
        Page<Category> categories;

        if (filterDto.getSearch() != null && !filterDto.getSearch().isEmpty()) {
            categories = categoryRepository.searchByNameOrDescription(filterDto.getSearch(), pageable);
        } else {
            categories = categoryRepository.findAll(pageable);
        }

        return categories.map(categoryMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID categoryId) {
        log.info("Deleting category: {}", categoryId);

        Category category = getCategoryById(categoryId);

        category.setIsActive(false);
        categoryRepository.save(category);

        // Soft delete all contents
        contentRepository.softDeleteByCategoryId(categoryId);

        // Publish event
        eventPublisher.publishCategoryDeleted(
                CategoryDeletedEvent.builder()
                        .categoryId(categoryId)
                        .partnerId(category.getPartnerId())
                        .categoryName(category.getName())
                        .timestamp(System.currentTimeMillis())
                        .build()
        );

        invalidateCache(category.getPartnerId());
        invalidateCategoryCache(categoryId);

        log.info("Category {} deleted", categoryId);
    }

    @Override
    @Transactional
    public CategoryDto createPartnerCategory(UUID partnerId, CreateCategoryDto dto) {
        log.info("Partner {} creating category: {}", partnerId, dto.getName());

        Category category = categoryMapper.toEntity(dto);
        category.setStatus(Category.CategoryStatus.PENDING_APPROVAL);
        category.setCreatedBy(Category.CreatedByRole.PARTNER);
        category.setPartnerId(partnerId);

        Category savedCategory = categoryRepository.save(category);

        // Publish event
        eventPublisher.publishCategoryCreated(
                CategoryCreatedEvent.builder()
                        .categoryId(savedCategory.getId())
                        .partnerId(partnerId)
                        .categoryType(savedCategory.getType().toString())
                        .categoryName(savedCategory.getName())
                        .timestamp(System.currentTimeMillis())
                        .build()
        );

        log.info("Partner category created: {}", savedCategory.getId());
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDto> getMyCategories(UUID partnerId, FilterCategoryDto filterDto) {
        log.debug("Getting categories for partner: {}", partnerId);

        Pageable pageable = PageRequest.of(filterDto.getPage(), filterDto.getLimit());
        Page<Category> categories = categoryRepository.findByPartnerIdAndIsActive(partnerId, true, pageable);

        return categories.map(categoryMapper::toDto);
    }

    @Override
    @Transactional
    public CategoryDto updateMyCategory(UUID partnerId, UUID categoryId, UpdateCategoryDto dto) {
        log.info("Partner {} updating category: {}", partnerId, categoryId);

        Category category = categoryRepository.findByIdAndPartnerIdAndIsActive(categoryId, partnerId, true)
                .orElseThrow(() -> new ForbiddenException("You are not the owner of this category"));

        // Cannot update if already approved or rejected
        if (category.getStatus() == Category.CategoryStatus.APPROVED ||
            category.getStatus() == Category.CategoryStatus.REJECTED) {
            throw new BadRequestException("Cannot update approved or rejected categories");
        }

        categoryMapper.updateEntityFromDto(dto, category);
        Category updatedCategory = categoryRepository.save(category);

        invalidateCategoryCache(categoryId);

        log.info("Category {} updated by partner {}", categoryId, partnerId);
        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteMyCategory(UUID partnerId, UUID categoryId) {
        log.info("Partner {} deleting category: {}", partnerId, categoryId);

        Category category = categoryRepository.findByIdAndPartnerIdAndIsActive(categoryId, partnerId, true)
                .orElseThrow(() -> new ForbiddenException("You are not the owner of this category"));

        category.setIsActive(false);
        categoryRepository.save(category);

        // Soft delete all contents
        contentRepository.softDeleteByCategoryId(categoryId);

        // Publish event
        eventPublisher.publishCategoryDeleted(
                CategoryDeletedEvent.builder()
                        .categoryId(categoryId)
                        .partnerId(partnerId)
                        .categoryName(category.getName())
                        .timestamp(System.currentTimeMillis())
                        .build()
        );

        invalidateCache(partnerId);
        invalidateCategoryCache(categoryId);

        log.info("Category {} deleted by partner {}", categoryId, partnerId);
    }

    @Override
    @Transactional
    public CategoryContentDto addContent(UUID categoryId, CreateCategoryContentDto dto) {
        log.info("Adding content to category: {}", categoryId);

        Category category = getCategoryById(categoryId);

        CategoryContent content = contentMapper.toEntity(dto);
        content.setCategoryId(categoryId);

        // Auto-assign order if not provided
        if (content.getOrder() == 0) {
            Integer maxOrder = contentRepository.getMaxOrderByCategoryId(categoryId);
            content.setOrder(maxOrder != null ? maxOrder + 1 : 1);
        }

        CategoryContent savedContent = contentRepository.save(content);

        invalidateCategoryCache(categoryId);

        log.info("Content {} added to category {}", savedContent.getId(), categoryId);
        return contentMapper.toDto(savedContent);
    }

    @Override
    @Transactional
    public CategoryContentDto updateContent(UUID contentId, UpdateCategoryContentDto dto) {
        log.info("Updating content: {}", contentId);

        CategoryContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new NotFoundException("Content not found"));

        contentMapper.updateEntityFromDto(dto, content);
        CategoryContent updatedContent = contentRepository.save(content);

        invalidateCategoryCache(content.getCategoryId());

        log.info("Content {} updated", contentId);
        return contentMapper.toDto(updatedContent);
    }

    @Override
    @Transactional
    public void deleteContent(UUID contentId) {
        log.info("Deleting content: {}", contentId);

        CategoryContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new NotFoundException("Content not found"));

        UUID categoryId = content.getCategoryId();
        content.setIsActive(false);
        contentRepository.save(content);

        invalidateCategoryCache(categoryId);

        log.info("Content {} deleted", contentId);
    }

    @Override
    @Transactional
    public void reorderContent(UUID categoryId, List<UUID> orderedIds) {
        log.info("Reordering content for category: {}", categoryId);

        Category category = getCategoryById(categoryId);

        List<CategoryContent> contents = contentRepository.findAllByCategoryId(categoryId);

        if (orderedIds.size() != contents.size()) {
            throw new BadRequestException("Invalid number of content IDs");
        }

        for (int i = 0; i < orderedIds.size(); i++) {
            UUID contentId = orderedIds.get(i);
            CategoryContent content = contents.stream()
                    .filter(c -> c.getId().equals(contentId))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Content not found: " + contentId));
            content.setOrder(i);
        }

        contentRepository.saveAll(contents);

        invalidateCategoryCache(categoryId);

        log.info("Content reordered for category {}", categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDto> getPublicCategoriesByPartner(UUID partnerId, FilterCategoryDto filterDto) {
        log.debug("Getting public categories for partner: {}", partnerId);

        // Try to get from cache (with resilience)
        String cacheKey = CACHE_CATEGORIES_BY_PARTNER + partnerId;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof Page) {
                log.debug("Returning cached public categories for partner: {}", partnerId);
                return (Page<CategoryDto>) cached;
            }
        } catch (Exception e) {
            log.warn("⚠️ Redis cache read failed for key {}: {}", cacheKey, e.getMessage());
        }

        Pageable pageable = PageRequest.of(filterDto.getPage(), filterDto.getLimit());
        Page<Category> categories = categoryRepository.findPublicApprovedByPartner(partnerId, pageable);
        Page<CategoryDto> result = categories.map(categoryMapper::toDto);

        // Cache the result (with resilience - don't fail if Redis is down)
        try {
            redisTemplate.opsForValue().set(cacheKey, result);
            redisTemplate.expire(cacheKey, java.time.Duration.ofSeconds(CACHE_TTL));
        } catch (Exception e) {
            log.warn("⚠️ Redis cache write failed for key {}: {}", cacheKey, e.getMessage());
        }

        log.debug("Public categories for partner {} retrieved and cached", partnerId);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryWithContentDto getCategoryWithContent(UUID categoryId) {
        log.debug("Getting category with content: {}", categoryId);

        // Try to get from cache (with resilience)
        String cacheKey = CACHE_CATEGORY_WITH_CONTENT + categoryId + CACHE_SUFFIX_FULL;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof CategoryWithContentDto) {
                log.debug("Returning cached category with content: {}", categoryId);
                return (CategoryWithContentDto) cached;
            }
        } catch (Exception e) {
            log.warn("⚠️ Redis cache read failed for key {}: {}", cacheKey, e.getMessage());
        }

        Category category = getCategoryById(categoryId);
        List<CategoryContent> contents = contentRepository.findByCategoryIdAndIsActiveOrderByOrderAsc(categoryId, true);

        CategoryWithContentDto dto = CategoryWithContentDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .coverImage(category.getCoverImage())
                .type(category.getType())
                .status(category.getStatus())
                .visibility(category.getVisibility())
                .createdBy(category.getCreatedBy())
                .partnerId(category.getPartnerId())
                .tags(category.getTags())
                .metadata(category.getMetadata())
                .contents(contents.stream().map(contentMapper::toDto).collect(Collectors.toList()))
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();

        // Cache the result (with resilience - don't fail if Redis is down)
        try {
            redisTemplate.opsForValue().set(cacheKey, dto);
            redisTemplate.expire(cacheKey, java.time.Duration.ofSeconds(CACHE_TTL));
        } catch (Exception e) {
            log.warn("⚠️ Redis cache write failed for key {}: {}", cacheKey, e.getMessage());
        }

        log.debug("Category with content {} retrieved and cached", categoryId);
        return dto;
    }

    @Override
    public Category getCategoryById(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found: " + categoryId));
    }

    @Override
    public boolean isPartnerOwner(UUID categoryId, UUID partnerId) {
        return categoryRepository.findByIdAndPartnerIdAndIsActive(categoryId, partnerId, true).isPresent();
    }

    // ===== PRIVATE HELPER METHODS =====

    private void invalidateCache(UUID partnerId) {
        if (partnerId != null) {
            String cacheKey = CACHE_CATEGORIES_BY_PARTNER + partnerId;
            try {
                redisTemplate.delete(cacheKey);
                log.debug("Invalidated cache for partner: {}", partnerId);
            } catch (Exception e) {
                log.warn("⚠️ Redis cache delete failed for key {}: {}", cacheKey, e.getMessage());
            }
        }
    }

    private void invalidateCategoryCache(UUID categoryId) {
        String cacheKey = CACHE_CATEGORY_WITH_CONTENT + categoryId + CACHE_SUFFIX_FULL;
        try {
            redisTemplate.delete(cacheKey);
            log.debug("Invalidated cache for category: {}", categoryId);
        } catch (Exception e) {
            log.warn("⚠️ Redis cache delete failed for key {}: {}", cacheKey, e.getMessage());
        }
    }
}

