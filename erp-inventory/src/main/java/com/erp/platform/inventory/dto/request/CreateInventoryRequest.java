package com.erp.platform.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInventoryRequest {

    @NotNull(message = "Le local est obligatoire")
    private UUID localId;

    @NotNull(message = "Le type d'inventaire est obligatoire")
    private String type;

    private List<UUID> productIds;
}
