package com.erp.platform.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockEntryResponse {

    private UUID id;
    private UUID productId;
    private String productRef;
    private String productCode;
    private String productName;
    private UUID localId;
    private String localName;
    private String localCode;
    private BigDecimal quantity;
    private BigDecimal reservedQty;
    private BigDecimal availableQty;
    private BigDecimal avgUnitCost;
    private BigDecimal totalValue;
    private String stockStatus; // "OK" | "LOW" | "OUT"
    private LocalDateTime lastMovementAt;
}
