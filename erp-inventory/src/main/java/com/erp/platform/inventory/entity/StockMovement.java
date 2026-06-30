package com.erp.platform.inventory.entity;

import com.erp.platform.core.common.BaseEntity;
import com.erp.platform.inventory.enums.MovementType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "local_id", nullable = false)
    private UUID localId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private MovementType type;

    @Column(name = "quantity", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_cost", precision = 14, scale = 4)
    private BigDecimal unitCost;

    @Column(name = "balance_after", nullable = false, precision = 14, scale = 3)
    private BigDecimal balanceAfter;

    @Column(name = "reason")
    private String reason;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "target_local_id")
    private UUID targetLocalId;

    @Column(name = "lot_id")
    private UUID lotId;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_by_name", length = 200)
    private String createdByName;
}
