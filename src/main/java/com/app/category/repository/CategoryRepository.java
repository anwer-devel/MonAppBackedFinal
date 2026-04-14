package com.app.category.repository;

import com.app.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    /**
     * Find all categories by partner ID
     */
    Page<Category> findByPartnerIdAndIsActive(UUID partnerId, Boolean isActive, Pageable pageable);

    /**
     * Find all public and approved categories by partner
     */
    @Query("SELECT c FROM Category c WHERE c.partnerId = :partnerId AND c.status = 'APPROVED' " +
            "AND c.visibility = 'PUBLIC' AND c.isActive = true")
    Page<Category> findPublicApprovedByPartner(@Param("partnerId") UUID partnerId, Pageable pageable);

    /**
     * Find all pending approval categories
     */
    Page<Category> findByStatusAndIsActive(Category.CategoryStatus status, Boolean isActive, Pageable pageable);

    /**
     * Find categories by status
     */
    List<Category> findByStatusAndIsActive(Category.CategoryStatus status, Boolean isActive);

    /**
     * Search categories by name or description
     */
    @Query("SELECT c FROM Category c WHERE " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND c.isActive = true")
    Page<Category> searchByNameOrDescription(@Param("search") String search, Pageable pageable);

    /**
     * Find all public and approved categories
     */
    @Query("SELECT c FROM Category c WHERE c.status = 'APPROVED' AND c.visibility = 'PUBLIC' " +
            "AND c.isActive = true")
    Page<Category> findAllPublicApproved(Pageable pageable);

    /**
     * Find categories by type
     */
    Page<Category> findByTypeAndStatusAndVisibilityAndIsActive(
            Category.CategoryType type,
            Category.CategoryStatus status,
            Category.CategoryVisibility visibility,
            Boolean isActive,
            Pageable pageable);

    /**
     * Count categories by status
     */
    long countByStatusAndIsActive(Category.CategoryStatus status, Boolean isActive);

    /**
     * Find by partner and status
     */
    Page<Category> findByPartnerIdAndStatusAndIsActive(UUID partnerId, Category.CategoryStatus status, Boolean isActive, Pageable pageable);

    /**
     * Check if category exists and is owned by partner
     */
    Optional<Category> findByIdAndPartnerIdAndIsActive(UUID id, UUID partnerId, Boolean isActive);
}

