package com.erp.platform.catalog.mapper;

import com.erp.platform.catalog.dto.response.*;
import com.erp.platform.catalog.entity.*;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface ProductMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categoryColor", source = "category.colorHex")
    @Mapping(target = "categoryIcon", source = "category.iconName")
    @Mapping(target = "unitId", source = "unit.id")
    @Mapping(target = "unitSymbol", source = "unit.symbol")
    @Mapping(target = "salePriceTTC", ignore = true)
    @Mapping(target = "currentStock", ignore = true)
    @Mapping(target = "stockStatus", ignore = true)
    ProductSummaryResponse toSummaryResponse(Product product);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categoryColor", source = "category.colorHex")
    @Mapping(target = "categoryIcon", source = "category.iconName")
    @Mapping(target = "unitId", source = "unit.id")
    @Mapping(target = "unitSymbol", source = "unit.symbol")
    @Mapping(target = "salePriceTTC", ignore = true)
    @Mapping(target = "currentStock", ignore = true)
    @Mapping(target = "stockStatus", ignore = true)
    @Mapping(target = "imageUrls", expression = "java(product.getImageUrls() != null ? java.util.Arrays.asList(product.getImageUrls()) : null)")
    ProductResponse toResponse(Product product);

    AutoPartExtensionResponse toAutoPartResponse(AutoPartExtension ext);

    VehicleCompatibilityResponse toCompatibilityResponse(VehicleCompatibility compat);

    PharmaExtensionResponse toPharmaResponse(PharmaExtension ext);

    HardwareExtensionResponse toHardwareResponse(HardwareExtension ext);

    @AfterMapping
    default void calculatePrices(Product product, @MappingTarget ProductSummaryResponse response) {
        if (product.getSalePriceHT() != null && product.getTaxRate() != null) {
            BigDecimal taxMultiplier = BigDecimal.ONE.add(product.getTaxRate().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
            response.setSalePriceTTC(product.getSalePriceHT().multiply(taxMultiplier).setScale(3, RoundingMode.HALF_UP));
        }
    }

    @AfterMapping
    default void calculatePricesFull(Product product, @MappingTarget ProductResponse response) {
        if (product.getSalePriceHT() != null && product.getTaxRate() != null) {
            BigDecimal taxMultiplier = BigDecimal.ONE.add(product.getTaxRate().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
            response.setSalePriceTTC(product.getSalePriceHT().multiply(taxMultiplier).setScale(3, RoundingMode.HALF_UP));
        }
    }
}
