package com.app.partner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePartnerRequest {
    @NotBlank(message = "Partner name is required")
    private String name;

    private String description;

    @NotNull(message = "Partner type is required")
    private String type;

    @NotNull(message = "Zone ID is required")
    private UUID zoneId;
}

