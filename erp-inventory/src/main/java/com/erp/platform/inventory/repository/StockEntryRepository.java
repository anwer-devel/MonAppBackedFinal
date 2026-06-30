package com.erp.platform.inventory.repository;

import com.erp.platform.inventory.entity.StockEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockEntryRepository extends JpaRepository<StockEntry, UUID> {

    Optional<StockEntry> findByProductIdAndLocalIdAndIsDeletedFalse(UUID productId, UUID localId);

    List<StockEntry> findByProductIdAndIsDeletedFalse(UUID productId);

    List<StockEntry> findByProductIdInAndIsDeletedFalse(Collection<UUID> productIds);

    List<StockEntry> findByLocalIdAndIsDeletedFalse(UUID localId);

    @Query("""
      SELECT se FROM StockEntry se
      WHERE se.localId = :localId
        AND se.isDeleted = false
        AND se.quantity <= :threshold
      """)
    List<StockEntry> findLowStockByLocal(
            @Param("localId") UUID localId,
            @Param("threshold") BigDecimal threshold);

    @Query("""
      SELECT COALESCE(SUM(se.quantity), 0) FROM StockEntry se
      WHERE se.productId = :productId AND se.isDeleted = false
      """)
    BigDecimal getTotalStockAcrossLocals(@Param("productId") UUID productId);
}
