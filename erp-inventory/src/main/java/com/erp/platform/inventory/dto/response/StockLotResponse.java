package com.erp.platform.inventory.dto.response;

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
public class StockLotResponse {

    private UUID id;
    private UUID productId;
    private String productRef;
    private String productCode;
    private String productName;
    private UUID localId;
    private String localName;
    private String lotNumber;
    private BigDecimal quantity;
    private LocalDate expiryDate;
    private LocalDate manufactureDate;
    private String supplierRef;
    private BigDecimal unitCost;
    private Integer daysUntilExpiry;
    private String expiryStatus; // "OK" | "SOON" | "EXPIRED"
}
