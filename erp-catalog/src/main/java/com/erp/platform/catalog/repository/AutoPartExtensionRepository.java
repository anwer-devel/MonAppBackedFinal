package com.erp.platform.catalog.repository;

import com.erp.platform.catalog.entity.AutoPartExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AutoPartExtensionRepository extends JpaRepository<AutoPartExtension, UUID> {
    Optional<AutoPartExtension> findByProduct_IdAndIsDeletedFalse(UUID productId);
}
