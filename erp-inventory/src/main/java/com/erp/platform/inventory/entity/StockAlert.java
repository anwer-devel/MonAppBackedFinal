package com.erp.platform.inventory.entity;

import com.erp.platform.core.common.BaseEntity;
import com.erp.platform.inventory.enums.AlertSeverity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_alerts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAlert extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "local_id", nullable = false)
    private UUID localId;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private AlertSeverity severity;

    @Column(name = "current_stock", precision = 14, scale = 3)
    private BigDecimal currentStock;

    @Column(name = "threshold", precision = 14, scale = 3)
    private BigDecimal threshold;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "lot_id")
    private UUID lotId;

    @Column(name = "is_resolved", nullable = false)
    @Builder.Default
    private Boolean isResolved = false;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
