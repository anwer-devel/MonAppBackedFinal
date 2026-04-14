package com.app.zone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneDto {
    private UUID id;
    private String name;
    private String description;
    private Double latitude;
    private Double longitude;
    private Integer partnerCount;
}

