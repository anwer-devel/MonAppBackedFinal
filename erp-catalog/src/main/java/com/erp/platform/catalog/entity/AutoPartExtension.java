package com.erp.platform.catalog.entity;

import com.erp.platform.catalog.enums.AutoFamily;
import com.erp.platform.core.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "auto_part_extensions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AutoPartExtension extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(name = "oem_ref", length = 100)
    private String oemRef;

    @Column(name = "aftermarket_ref", length = 100)
    private String aftermarketRef;

    @Column(name = "brand", length = 100)
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(name = "family_enum", length = 30)
    private AutoFamily familyEnum;

    @Column(name = "technical_note", columnDefinition = "TEXT")
    private String technicalNote;

    @Column(name = "data_sheet_url", length = 500)
    private String dataSheetUrl;

    @Column(name = "is_oem_equivalent", nullable = false)
    @Builder.Default
    private boolean isOemEquivalent = false;

    @OneToMany(mappedBy = "autoPart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<VehicleCompatibility> compatibilities = new ArrayList<>();
}
