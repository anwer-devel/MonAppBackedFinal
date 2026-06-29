package com.erp.platform.catalog.repository;

import com.erp.platform.catalog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByParentIsNullAndIsActiveTrueAndIsDeletedFalse();
    List<Category> findByParent_IdAndIsActiveTrueAndIsDeletedFalse(UUID parentId);
    Optional<Category> findByCodeIgnoreCaseAndIsDeletedFalse(String code);
}
