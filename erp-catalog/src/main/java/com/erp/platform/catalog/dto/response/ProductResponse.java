package com.erp.platform.catalog.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductResponse {
    private UUID id;
    private String ref;
    private String barcode;
    private String name;
    private String shortDescription;
    private UUID categoryId;
    private String categoryName;
    private String categoryColor;
    private String categoryIcon;
    private UUID unitId;
    private String unitSymbol;
    private String sectorType;
    private BigDecimal purchasePriceHT;
    private BigDecimal salePriceHT;
    private BigDecimal taxRate;
    private BigDecimal salePriceTTC;
    private BigDecimal marginRate;
    private boolean isActive;
    private boolean isFavorite;
    private boolean trackStock;
    private Integer currentStock;
    private String stockStatus;

    private String description;
    private List<String> imageUrls;
    private Map<String, String> attributes;
    private int minStockLevel;
    private int safetyStockLevel;
    private int maxStockLevel;
    private AutoPartExtensionResponse autoPartExt;
    private PharmaExtensionResponse pharmaExt;
    private HardwareExtensionResponse hardwareExt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
