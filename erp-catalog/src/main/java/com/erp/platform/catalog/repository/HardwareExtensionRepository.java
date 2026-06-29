package com.erp.platform.catalog.repository;

import com.erp.platform.catalog.entity.HardwareExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HardwareExtensionRepository extends JpaRepository<HardwareExtension, UUID> {
    Optional<HardwareExtension> findByProduct_IdAndIsDeletedFalse(UUID productId);
}
