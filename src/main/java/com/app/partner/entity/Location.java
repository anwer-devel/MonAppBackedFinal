package com.app.partner.entity;

import com.app.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "locations", indexes = {
        @Index(name = "idx_location_partner", columnList = "partner_id", unique = true),
        @Index(name = "idx_location_active", columnList = "is_active")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @NotNull
    @Column(nullable = false)
    private Double latitude;

    @NotNull
    @Column(nullable = false)
    private Double longitude;

    @Builder.Default
    @Column(nullable = false)
    private String address = "";

    @Builder.Default
    @Column(nullable = false)
    private String city = "";

    @Builder.Default
    @Column(nullable = false)
    private String country = "";
}

