package com.erp.platform.inventory.dto.request;

import jakarta.validation.constraints.DecimalMin;
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
public class StockTransferRequest {

    @NotNull(message = "Le produit est obligatoire")
    private UUID productId;

    @NotNull(message = "Le local source est obligatoire")
    private UUID sourceLocalId;

    @NotNull(message = "Le local destination est obligatoire")
    private UUID targetLocalId;

    @NotNull(message = "La quantité est obligatoire")
    @DecimalMin(value = "0.001", message = "La quantité doit être positive")
    private BigDecimal quantity;

    private String reason;
}
