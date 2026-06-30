package com.erp.platform.inventory.entity;

import com.erp.platform.core.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "physical_inventory_lines")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalInventoryLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private PhysicalInventory inventory;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "expected_qty", nullable = false, precision = 14, scale = 3)
    private BigDecimal expectedQty;

    @Column(name = "counted_qty", precision = 14, scale = 3)
    private BigDecimal countedQty;

    @Column(name = "variance_qty", precision = 14, scale = 3)
    private BigDecimal varianceQty;

    @Column(name = "is_counted", nullable = false)
    @Builder.Default
    private boolean isCounted = false;
}
