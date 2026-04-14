package com.app.zone.entity;

import com.app.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "zones", indexes = {
        @Index(name = "idx_zone_name", columnList = "name"),
        @Index(name = "idx_zone_active", columnList = "is_active")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Zone extends BaseEntity {

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Double latitude = 0.0;

    @Builder.Default
    @Column(nullable = false)
    private Double longitude = 0.0;

    @Builder.Default
    @Column(nullable = false)
    private Integer partnerCount = 0;
}

