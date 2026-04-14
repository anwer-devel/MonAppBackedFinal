package com.app.partner.dto;

import com.app.partner.entity.Partner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerDto {
    private UUID id;
    private String name;
    private String description;
    private Partner.PartnerType type;
    private Double rating;
    private Boolean isVerified;
    private UUID zoneId;
    private UUID ownerId;
    private Integer assetCount;
}

