package com.erp.platform.inventory.dto.request;

import com.erp.platform.inventory.enums.MovementType;
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
public class CreateMovementRequest {

    @NotNull(message = "Le produit est obligatoire")
    private UUID productId;

    @NotNull(message = "Le local est obligatoire")
    private UUID localId;

    @NotNull(message = "Le type de mouvement est obligatoire")
    private MovementType type;

    @NotNull(message = "La quantité est obligatoire")
    @DecimalMin(value = "0.001", message = "La quantité doit être positive")
    private BigDecimal quantity;

    private BigDecimal unitCost;

    private String reason;

    private String referenceType;

    private UUID referenceId;

    private String lotNumber;
}
