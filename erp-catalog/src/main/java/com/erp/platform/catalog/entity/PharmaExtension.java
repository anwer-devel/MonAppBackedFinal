package com.erp.platform.catalog.entity;

import com.erp.platform.catalog.enums.GalenicForm;
import com.erp.platform.catalog.enums.StorageTemp;
import com.erp.platform.core.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pharma_extensions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PharmaExtension extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(name = "dci", length = 200)
    private String dci;

    @Column(name = "dosage", length = 100)
    private String dosage;

    @Enumerated(EnumType.STRING)
    @Column(name = "galenic", length = 50)
    private GalenicForm galenic;

    @Column(name = "therapeutic_class", length = 200)
    private String therapeuticClass;

    @Column(name = "amm_number", length = 100)
    private String ammNumber;

    @Column(name = "requires_prescription", nullable = false)
    @Builder.Default
    private boolean requiresPrescription = false;

    @Column(name = "is_generic", nullable = false)
    @Builder.Default
    private boolean isGeneric = false;

    @Column(name = "princeps_ref", length = 100)
    private String princepsRef;

    @Column(name = "laboratory_name", length = 200)
    private String laboratoryName;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_temp", nullable = false, length = 20)
    @Builder.Default
    private StorageTemp storageTemp = StorageTemp.AMBIENT;

    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;
}
