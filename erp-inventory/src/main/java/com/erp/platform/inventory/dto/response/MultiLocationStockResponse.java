package com.erp.platform.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiLocationStockResponse {

    private UUID productId;
    private String productRef;
    private String productCode;
    private String productName;
    private String categoryName;
    private Map<String, BigDecimal> quantityByLocal;
    private BigDecimal totalQuantity;
    private BigDecimal totalAvailable;
    private String stockStatus;
    private BigDecimal valuation;
    private BigDecimal salePriceHT;
    private List<LocalStockDetail> locations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocalStockDetail {
        private UUID localId;
        private String localName;
        private BigDecimal quantity;
        private BigDecimal availableQty;
    }
}
