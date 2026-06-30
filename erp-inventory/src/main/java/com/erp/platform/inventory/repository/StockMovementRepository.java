package com.erp.platform.inventory.repository;

import com.erp.platform.inventory.entity.StockMovement;
import com.erp.platform.inventory.enums.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    Page<StockMovement> findByProductIdAndIsDeletedFalseOrderByCreatedAtDesc(
            UUID productId, Pageable pageable);

    Page<StockMovement> findByLocalIdAndIsDeletedFalseOrderByCreatedAtDesc(
            UUID localId, Pageable pageable);

    @Query("""
      SELECT m FROM StockMovement m
      WHERE m.isDeleted = false
        AND (:productId IS NULL OR m.productId = :productId)
        AND (:localId IS NULL OR m.localId = :localId)
        AND (:type IS NULL OR m.type = :type)
        AND (:dateFrom IS NULL OR m.createdAt >= :dateFrom)
        AND (:dateTo IS NULL OR m.createdAt <= :dateTo)
      ORDER BY m.createdAt DESC
      """)
    Page<StockMovement> findWithFilters(
            @Param("productId") UUID productId,
            @Param("localId") UUID localId,
            @Param("type") MovementType type,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable);

    List<StockMovement> findByReferenceTypeAndReferenceIdAndIsDeletedFalse(
            String referenceType, UUID referenceId);
}
