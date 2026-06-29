package com.erp.platform.catalog.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UpdateProductRequest {
    @Size(max = 100)
    private String ref;
    private String barcode;
    @Size(max = 300)
    private String name;
    private String description;
    private String shortDescription;
    private UUID categoryId;
    private UUID unitId;
    private String sectorType;
    @DecimalMin("0")
    private BigDecimal purchasePriceHT;
    @DecimalMin("0")
    private BigDecimal salePriceHT;
    @DecimalMin("0") @DecimalMax("100")
    private BigDecimal taxRate;
    private Integer minStockLevel;
    private Integer safetyStockLevel;
    private Integer maxStockLevel;
    private Boolean trackStock;
    private Boolean isActive;
    private Boolean isFavorite;
    private List<String> imageUrls;
    private Map<String, String> attributes;
    private AutoPartExtensionRequest autoPartExt;
    private PharmaExtensionRequest pharmaExt;
    private HardwareExtensionRequest hardwareExt;
}
