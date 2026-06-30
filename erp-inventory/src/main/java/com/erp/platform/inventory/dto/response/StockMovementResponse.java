package com.erp.platform.inventory.dto.response;

import com.erp.platform.inventory.enums.MovementType;
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
public class StockMovementResponse {

    private UUID id;
    private UUID productId;
    private String productRef;
    private String productCode;
    private String productName;
    private UUID localId;
    private String localName;
    private MovementType type;
    private String typeLabel;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal balanceAfter;
    private String reason;
    private String referenceType;
    private UUID referenceId;
    private UUID targetLocalId;
    private String targetLocalName;
    private String lotNumber;
    private UUID createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
}
