package com.app.partner.dto;

import com.app.partner.entity.PartnerAsset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerAssetDto {
    private UUID id;
    private PartnerAsset.AssetType type;
    private String url;
    private String metadataJson;
    private Integer displayOrder;
}

