package com.erp.platform.inventory.entity;

import com.erp.platform.core.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "stock_lots",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "local_id", "lot_number"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLot extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "local_id", nullable = false)
    private UUID localId;

    @Column(name = "lot_number", nullable = false, length = 100)
    private String lotNumber;

    @Column(name = "quantity", nullable = false, precision = 14, scale = 3)
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "supplier_ref", length = 100)
    private String supplierRef;

    @Column(name = "unit_cost", precision = 14, scale = 4)
    private BigDecimal unitCost;
}
