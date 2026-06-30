package com.erp.platform.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentRequest {

    @NotNull(message = "Le produit est obligatoire")
    private UUID productId;

    @NotNull(message = "Le local est obligatoire")
    private UUID localId;

    @NotNull(message = "La nouvelle quantité est obligatoire")
    private BigDecimal newQuantity;

    @NotBlank(message = "Le motif d'ajustement est obligatoire")
    private String reason;
}
