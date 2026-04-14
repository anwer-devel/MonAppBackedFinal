package com.app.partner.entity;

import com.app.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "partner_assets", indexes = {
        @Index(name = "idx_asset_partner", columnList = "partner_id"),
        @Index(name = "idx_asset_type", columnList = "type"),
        @Index(name = "idx_asset_active", columnList = "is_active")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerAsset extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType type;

    @NotBlank
    @Column(nullable = false)
    private String url;

    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    @Builder.Default
    @Column(nullable = false)
    private Integer displayOrder = 0;

    public enum AssetType {
        IMAGE,
        COVER,
        LOGO
    }
}

