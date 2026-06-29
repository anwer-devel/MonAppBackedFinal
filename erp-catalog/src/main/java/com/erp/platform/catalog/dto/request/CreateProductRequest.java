package com.erp.platform.catalog.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateProductRequest {
    @NotBlank @Size(max = 100)
    private String ref;
    private String barcode;
    @NotBlank @Size(max = 300)
    private String name;
    private String description;
    private String shortDescription;
    private UUID categoryId;
    private UUID unitId;
    private String sectorType;
    @NotNull @DecimalMin("0")
    private BigDecimal purchasePriceHT;
    @NotNull @DecimalMin("0")
    private BigDecimal salePriceHT;
    @DecimalMin("0") @DecimalMax("100")
    private BigDecimal taxRate;
    private int minStockLevel;
    private int safetyStockLevel;
    private int maxStockLevel;
    @Builder.Default
    private boolean trackStock = true;
    @Builder.Default
    private boolean isActive = true;
    @Builder.Default
    private boolean isFavorite = false;
    private List<String> imageUrls;
    private Map<String, String> attributes;
    private AutoPartExtensionRequest autoPartExt;
    private PharmaExtensionRequest pharmaExt;
    private HardwareExtensionRequest hardwareExt;
}
