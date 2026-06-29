package com.erp.platform.catalog.repository;

import com.erp.platform.catalog.entity.PharmaExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PharmaExtensionRepository extends JpaRepository<PharmaExtension, UUID> {
    Optional<PharmaExtension> findByProduct_IdAndIsDeletedFalse(UUID productId);
}
