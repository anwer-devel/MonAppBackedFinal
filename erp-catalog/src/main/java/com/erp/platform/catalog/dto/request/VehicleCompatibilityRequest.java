package com.erp.platform.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class VehicleCompatibilityRequest {
    @NotBlank
    private String vehicleMake;
    @NotBlank
    private String vehicleModel;
    private String vehicleVariant;
    private Integer yearFrom;
    private Integer yearTo;
    private String engineType;
    private String vinPattern;
    private String notes;
}
