package com.erp.platform.inventory.entity;

import com.erp.platform.core.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_entries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "local_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockEntry extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "local_id", nullable = false)
    private UUID localId;

    @Column(name = "quantity", nullable = false, precision = 14, scale = 3)
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "reserved_qty", nullable = false, precision = 14, scale = 3)
    @Builder.Default
    private BigDecimal reservedQty = BigDecimal.ZERO;

    @Column(name = "avg_unit_cost", nullable = false, precision = 14, scale = 4)
    @Builder.Default
    private BigDecimal avgUnitCost = BigDecimal.ZERO;

    @Column(name = "last_movement_at")
    private LocalDateTime lastMovementAt;

    @Transient
    public BigDecimal getAvailableQty() {
        BigDecimal q = quantity != null ? quantity : BigDecimal.ZERO;
        BigDecimal r = reservedQty != null ? reservedQty : BigDecimal.ZERO;
        return q.subtract(r);
    }
}
