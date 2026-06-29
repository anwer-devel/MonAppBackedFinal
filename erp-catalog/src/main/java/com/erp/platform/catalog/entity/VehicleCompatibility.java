package com.erp.platform.catalog.entity;

import com.erp.platform.core.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicle_compatibilities")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class VehicleCompatibility extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auto_part_id", nullable = false)
    private AutoPartExtension autoPart;

    @Column(name = "vehicle_make", nullable = false, length = 100)
    private String vehicleMake;

    @Column(name = "vehicle_model", nullable = false, length = 100)
    private String vehicleModel;

    @Column(name = "vehicle_variant", length = 100)
    private String vehicleVariant;

    @Column(name = "year_from")
    private Integer yearFrom;

    @Column(name = "year_to")
    private Integer yearTo;

    @Column(name = "engine_type", length = 20)
    private String engineType;

    @Column(name = "vin_pattern", length = 50)
    private String vinPattern;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
