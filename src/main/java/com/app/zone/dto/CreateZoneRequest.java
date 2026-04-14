package com.app.zone.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateZoneRequest {
    @NotBlank(message = "Zone name is required")
    private String name;

    private String description;

    @Builder.Default
    private Double latitude = 0.0;

    @Builder.Default
    private Double longitude = 0.0;
}

