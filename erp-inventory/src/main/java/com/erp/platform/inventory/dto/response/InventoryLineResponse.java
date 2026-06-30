package com.erp.platform.inventory.dto.response;

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
public class InventoryLineResponse {

    private UUID id;
    private UUID productId;
    private String productRef;
    private String productCode;
    private String productName;
    private BigDecimal expectedQty;
    private BigDecimal countedQty;
    private BigDecimal varianceQty;
    private boolean isCounted;
}
