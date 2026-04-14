package com.app.category.repository;

import com.app.category.entity.CategoryContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryContentRepository extends JpaRepository<CategoryContent, UUID> {

    /**
     * Find all contents for a category ordered by order
     */
    List<CategoryContent> findByCategoryIdAndIsActiveOrderByOrderAsc(UUID categoryId, Boolean isActive);

    /**
     * Find contents for a category with pagination
     */
    @Query("SELECT c FROM CategoryContent c WHERE c.categoryId = :categoryId AND c.isActive = true ORDER BY c.order ASC")
    List<CategoryContent> findAllByCategoryId(@Param("categoryId") UUID categoryId);

    /**
     * Count contents in a category
     */
    long countByCategoryIdAndIsActive(UUID categoryId, Boolean isActive);

    /**
     * Find content by ID and category ID
     */
    Optional<CategoryContent> findByIdAndCategoryIdAndIsActive(UUID id, UUID categoryId, Boolean isActive);

    /**
     * Delete all contents for a category (soft delete)
     */
    @Query("UPDATE CategoryContent c SET c.isActive = false WHERE c.categoryId = :categoryId AND c.isActive = true")
    void softDeleteByCategoryId(@Param("categoryId") UUID categoryId);

    /**
     * Get max order for a category
     */
    @Query("SELECT COALESCE(MAX(c.order), 0) FROM CategoryContent c WHERE c.categoryId = :categoryId AND c.isActive = true")
    Integer getMaxOrderByCategoryId(@Param("categoryId") UUID categoryId);
}

