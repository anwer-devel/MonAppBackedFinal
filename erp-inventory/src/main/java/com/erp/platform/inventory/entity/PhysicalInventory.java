package com.erp.platform.inventory.entity;

import com.erp.platform.core.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "physical_inventories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalInventory extends BaseEntity {

    @Column(name = "local_id", nullable = false)
    private UUID localId;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "IN_PROGRESS";

    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private String type = "PARTIAL";

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "total_items", nullable = false)
    @Builder.Default
    private int totalItems = 0;

    @Column(name = "total_variance_value", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalVarianceValue = BigDecimal.ZERO;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<PhysicalInventoryLine> lines = new ArrayList<>();
}
