package com.erp.platform.catalog.dto.response;

import lombok.*;

import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class VehicleCompatibilityResponse {
    private UUID id;
    private String vehicleMake;
    private String vehicleModel;
    private String vehicleVariant;
    private Integer yearFrom;
    private Integer yearTo;
    private String engineType;
    private String vinPattern;
    private String notes;
}
