package com.erp.platform.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInventoryLineRequest {

    @NotNull(message = "La quantité comptée est obligatoire")
    private BigDecimal countedQty;
}
