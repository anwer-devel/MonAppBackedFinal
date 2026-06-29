package com.erp.platform.catalog.entity;

import com.erp.platform.catalog.enums.HardwareFamily;
import com.erp.platform.core.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hardware_extensions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class HardwareExtension extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "family_enum", length = 30)
    private HardwareFamily familyEnum;

    @Column(name = "material", length = 100)
    private String material;

    @Column(name = "dimensions", length = 100)
    private String dimensions;

    @Column(name = "norm", length = 100)
    private String norm;

    @Column(name = "conditioning", length = 30)
    private String conditioning;

    @Column(name = "conditioning_qty", nullable = false)
    @Builder.Default
    private int conditioningQty = 1;

    @Column(name = "is_professional", nullable = false)
    @Builder.Default
    private boolean isProfessional = false;

    @Column(name = "color_or_finish", length = 100)
    private String colorOrFinish;
}
