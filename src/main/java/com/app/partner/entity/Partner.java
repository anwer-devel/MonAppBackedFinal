package com.app.partner.entity;

import com.app.auth.entity.User;
import com.app.common.entity.BaseEntity;
import com.app.zone.entity.Zone;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "partners", indexes = {
        @Index(name = "idx_partner_zone", columnList = "zone_id"),
        @Index(name = "idx_partner_owner", columnList = "owner_id"),
        @Index(name = "idx_partner_name", columnList = "name"),
        @Index(name = "idx_partner_active", columnList = "is_active")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partner extends BaseEntity {

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartnerType type;

    @Builder.Default
    @Column(nullable = false)
    private Double rating = 0.0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isVerified = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Builder.Default
    @Column(nullable = false)
    private Integer assetCount = 0;

    // ===== Flutter Cafe fields =====

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(length = 20)
    private String phone;

    @Builder.Default
    @Column(name = "is_open")
    private Boolean isOpen = true;

    @Builder.Default
    private Double price = 0.0;

    @Builder.Default
    private Integer likes = 0;


    @Builder.Default
    @Column(name = "is_trending")
    private Boolean isTrending = false;

    @Builder.Default
    private Integer reviews = 0;

    public enum PartnerType {
        CAFE,
        RESTAURANT,
        STORE,
        HOTEL,
        OTHER
    }
}

