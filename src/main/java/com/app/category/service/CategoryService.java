package com.app.category.service;

import com.app.category.dto.*;
import com.app.category.entity.Category;
import com.app.category.entity.CategoryContent;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

/**
 * Category management service interface.
 */
public interface CategoryService {

    // ===== ADMIN Methods =====

    /**
     * Create a public category by admin (auto-approved)
     */
    CategoryDto createPublicCategory(CreateCategoryDto dto);

    /**
     * Approve or reject a pending category
     */
    CategoryDto approveCategory(UUID categoryId, ApproveCategoryDto dto);

    /**
     * Get all categories with filtering and pagination (admin view)
     */
    Page<CategoryDto> getAllCategories(FilterCategoryDto filterDto);

    /**
     * Delete category (soft delete)
     */
    void deleteCategory(UUID categoryId);

    // ===== PARTNER Methods =====

    /**
     * Create a category by partner (pending approval)
     */
    CategoryDto createPartnerCategory(UUID partnerId, CreateCategoryDto dto);

    /**
     * Get all categories for a partner
     */
    Page<CategoryDto> getMyCategories(UUID partnerId, FilterCategoryDto filterDto);

    /**
     * Update a partner's category
     */
    CategoryDto updateMyCategory(UUID partnerId, UUID categoryId, UpdateCategoryDto dto);

    /**
     * Delete partner's category (soft delete)
     */
    void deleteMyCategory(UUID partnerId, UUID categoryId);

    // ===== CONTENT Methods =====

    /**
     * Add content to a category
     */
    CategoryContentDto addContent(UUID categoryId, CreateCategoryContentDto dto);

    /**
     * Update category content
     */
    CategoryContentDto updateContent(UUID contentId, UpdateCategoryContentDto dto);

    /**
     * Delete category content (soft delete)
     */
    void deleteContent(UUID contentId);

    /**
     * Reorder contents in a category
     */
    void reorderContent(UUID categoryId, List<UUID> orderedIds);

    // ===== PUBLIC Methods =====

    /**
     * Get public and approved categories for a partner
     */
    Page<CategoryDto> getPublicCategoriesByPartner(UUID partnerId, FilterCategoryDto filterDto);

    /**
     * Get category details with full content
     */
    CategoryWithContentDto getCategoryWithContent(UUID categoryId);

    // ===== UTILITY Methods =====

    /**
     * Get category by ID (internal use)
     */
    Category getCategoryById(UUID categoryId);

    /**
     * Check if user is owner of category
     */
    boolean isPartnerOwner(UUID categoryId, UUID partnerId);
}

