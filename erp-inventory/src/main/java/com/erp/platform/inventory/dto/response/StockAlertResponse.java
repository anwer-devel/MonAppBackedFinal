package com.erp.platform.inventory.dto.response;

import com.erp.platform.inventory.enums.AlertSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAlertResponse {

    private UUID id;
    private UUID productId;
    private String productRef;
    private String productCode;
    private String productName;
    private UUID localId;
    private String localName;
    private String type;
    private String typeLabel;
    private AlertSeverity severity;
    private BigDecimal currentStock;
    private BigDecimal threshold;
    private LocalDate expiryDate;
    private UUID lotId;
    private String lotNumber;
    private boolean isResolved;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
}
