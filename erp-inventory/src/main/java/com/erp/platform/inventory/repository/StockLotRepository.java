package com.erp.platform.inventory.repository;

import com.erp.platform.inventory.entity.StockLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockLotRepository extends JpaRepository<StockLot, UUID> {

    List<StockLot> findByProductIdAndLocalIdAndIsDeletedFalseOrderByExpiryDateAsc(
            UUID productId, UUID localId);

    @Query("""
      SELECT l FROM StockLot l
      WHERE l.isDeleted = false
        AND l.quantity > 0
        AND l.expiryDate IS NOT NULL
        AND l.expiryDate <= :beforeDate
      ORDER BY l.expiryDate ASC
      """)
    List<StockLot> findExpiringSoon(@Param("beforeDate") LocalDate beforeDate);

    Optional<StockLot> findByProductIdAndLocalIdAndLotNumberAndIsDeletedFalse(
            UUID productId, UUID localId, String lotNumber);
}
