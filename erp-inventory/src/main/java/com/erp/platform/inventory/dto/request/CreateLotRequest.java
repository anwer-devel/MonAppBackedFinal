package com.erp.platform.inventory.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLotRequest {

    @NotNull(message = "Le produit est obligatoire")
    private UUID productId;

    @NotNull(message = "Le local est obligatoire")
    private UUID localId;

    @NotBlank(message = "Le numéro de lot est obligatoire")
    private String lotNumber;

    @NotNull(message = "La quantité est obligatoire")
    @DecimalMin(value = "0.001", message = "La quantité doit être positive")
    private BigDecimal quantity;

    private LocalDate expiryDate;

    private LocalDate manufactureDate;

    private String supplierRef;

    private BigDecimal unitCost;
}
