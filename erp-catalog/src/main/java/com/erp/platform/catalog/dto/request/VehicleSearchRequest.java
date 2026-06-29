package com.erp.platform.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class VehicleSearchRequest {
    @NotBlank
    private String make;
    @NotBlank
    private String model;
    private Integer year;
    private String engineType;
}
