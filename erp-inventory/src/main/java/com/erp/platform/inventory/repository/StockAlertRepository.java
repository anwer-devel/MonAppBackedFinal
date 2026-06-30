package com.erp.platform.inventory.repository;

import com.erp.platform.inventory.entity.StockAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockAlertRepository extends JpaRepository<StockAlert, UUID> {

    List<StockAlert> findByIsResolvedFalseAndIsDeletedFalse();

    List<StockAlert> findByLocalIdAndIsResolvedFalseAndIsDeletedFalse(UUID localId);

    Optional<StockAlert> findByProductIdAndLocalIdAndTypeAndIsResolvedFalseAndIsDeletedFalse(
            UUID productId, UUID localId, String type);

    long countByIsResolvedFalseAndIsDeletedFalse();

    long countByLocalIdAndIsResolvedFalseAndIsDeletedFalse(UUID localId);
}
