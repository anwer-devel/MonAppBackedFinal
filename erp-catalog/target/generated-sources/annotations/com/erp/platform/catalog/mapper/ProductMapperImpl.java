package com.erp.platform.catalog.mapper;

import com.erp.platform.catalog.dto.response.AutoPartExtensionResponse;
import com.erp.platform.catalog.dto.response.HardwareExtensionResponse;
import com.erp.platform.catalog.dto.response.PharmaExtensionResponse;
import com.erp.platform.catalog.dto.response.ProductResponse;
import com.erp.platform.catalog.dto.response.ProductSummaryResponse;
import com.erp.platform.catalog.dto.response.VehicleCompatibilityResponse;
import com.erp.platform.catalog.entity.AutoPartExtension;
import com.erp.platform.catalog.entity.Category;
import com.erp.platform.catalog.entity.HardwareExtension;
import com.erp.platform.catalog.entity.PharmaExtension;
import com.erp.platform.catalog.entity.Product;
import com.erp.platform.catalog.entity.Unit;
import com.erp.platform.catalog.entity.VehicleCompatibility;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-29T15:49:23+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Amazon.com Inc.)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public ProductSummaryResponse toSummaryResponse(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductSummaryResponse productSummaryResponse = new ProductSummaryResponse();

        productSummaryResponse.setCategoryId( productCategoryId( product ) );
        productSummaryResponse.setCategoryName( productCategoryName( product ) );
        productSummaryResponse.setCategoryColor( productCategoryColorHex( product ) );
        productSummaryResponse.setCategoryIcon( productCategoryIconName( product ) );
        productSummaryResponse.setUnitId( productUnitId( product ) );
        productSummaryResponse.setUnitSymbol( productUnitSymbol( product ) );
        productSummaryResponse.setId( product.getId() );
        productSummaryResponse.setRef( product.getRef() );
        productSummaryResponse.setBarcode( product.getBarcode() );
        productSummaryResponse.setName( product.getName() );
        productSummaryResponse.setShortDescription( product.getShortDescription() );
        productSummaryResponse.setSectorType( product.getSectorType() );
        productSummaryResponse.setPurchasePriceHT( product.getPurchasePriceHT() );
        productSummaryResponse.setSalePriceHT( product.getSalePriceHT() );
        productSummaryResponse.setTaxRate( product.getTaxRate() );
        productSummaryResponse.setMarginRate( product.getMarginRate() );
        productSummaryResponse.setActive( product.isActive() );
        productSummaryResponse.setFavorite( product.isFavorite() );
        productSummaryResponse.setTrackStock( product.isTrackStock() );

        calculatePrices( product, productSummaryResponse );

        return productSummaryResponse;
    }

    @Override
    public ProductResponse toResponse(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductResponse productResponse = new ProductResponse();

        productResponse.setCategoryId( productCategoryId( product ) );
        productResponse.setCategoryName( productCategoryName( product ) );
        productResponse.setCategoryColor( productCategoryColorHex( product ) );
        productResponse.setCategoryIcon( productCategoryIconName( product ) );
        productResponse.setUnitId( productUnitId( product ) );
        productResponse.setUnitSymbol( productUnitSymbol( product ) );
        productResponse.setId( product.getId() );
        productResponse.setRef( product.getRef() );
        productResponse.setBarcode( product.getBarcode() );
        productResponse.setName( product.getName() );
        productResponse.setShortDescription( product.getShortDescription() );
        productResponse.setSectorType( product.getSectorType() );
        productResponse.setPurchasePriceHT( product.getPurchasePriceHT() );
        productResponse.setSalePriceHT( product.getSalePriceHT() );
        productResponse.setTaxRate( product.getTaxRate() );
        productResponse.setMarginRate( product.getMarginRate() );
        productResponse.setActive( product.isActive() );
        productResponse.setFavorite( product.isFavorite() );
        productResponse.setTrackStock( product.isTrackStock() );
        productResponse.setDescription( product.getDescription() );
        Map<String, String> map = product.getAttributes();
        if ( map != null ) {
            productResponse.setAttributes( new LinkedHashMap<String, String>( map ) );
        }
        productResponse.setMinStockLevel( product.getMinStockLevel() );
        productResponse.setSafetyStockLevel( product.getSafetyStockLevel() );
        productResponse.setMaxStockLevel( product.getMaxStockLevel() );
        productResponse.setAutoPartExt( toAutoPartResponse( product.getAutoPartExt() ) );
        productResponse.setPharmaExt( toPharmaResponse( product.getPharmaExt() ) );
        productResponse.setHardwareExt( toHardwareResponse( product.getHardwareExt() ) );
        productResponse.setCreatedAt( product.getCreatedAt() );
        productResponse.setUpdatedAt( product.getUpdatedAt() );

        productResponse.setImageUrls( product.getImageUrls() != null ? java.util.Arrays.asList(product.getImageUrls()) : null );

        calculatePricesFull( product, productResponse );

        return productResponse;
    }

    @Override
    public AutoPartExtensionResponse toAutoPartResponse(AutoPartExtension ext) {
        if ( ext == null ) {
            return null;
        }

        AutoPartExtensionResponse autoPartExtensionResponse = new AutoPartExtensionResponse();

        autoPartExtensionResponse.setId( ext.getId() );
        autoPartExtensionResponse.setOemRef( ext.getOemRef() );
        autoPartExtensionResponse.setAftermarketRef( ext.getAftermarketRef() );
        autoPartExtensionResponse.setBrand( ext.getBrand() );
        if ( ext.getFamilyEnum() != null ) {
            autoPartExtensionResponse.setFamilyEnum( ext.getFamilyEnum().name() );
        }
        autoPartExtensionResponse.setTechnicalNote( ext.getTechnicalNote() );
        autoPartExtensionResponse.setDataSheetUrl( ext.getDataSheetUrl() );
        autoPartExtensionResponse.setOemEquivalent( ext.isOemEquivalent() );
        autoPartExtensionResponse.setCompatibilities( vehicleCompatibilityListToVehicleCompatibilityResponseList( ext.getCompatibilities() ) );

        return autoPartExtensionResponse;
    }

    @Override
    public VehicleCompatibilityResponse toCompatibilityResponse(VehicleCompatibility compat) {
        if ( compat == null ) {
            return null;
        }

        VehicleCompatibilityResponse vehicleCompatibilityResponse = new VehicleCompatibilityResponse();

        vehicleCompatibilityResponse.setId( compat.getId() );
        vehicleCompatibilityResponse.setVehicleMake( compat.getVehicleMake() );
        vehicleCompatibilityResponse.setVehicleModel( compat.getVehicleModel() );
        vehicleCompatibilityResponse.setVehicleVariant( compat.getVehicleVariant() );
        vehicleCompatibilityResponse.setYearFrom( compat.getYearFrom() );
        vehicleCompatibilityResponse.setYearTo( compat.getYearTo() );
        vehicleCompatibilityResponse.setEngineType( compat.getEngineType() );
        vehicleCompatibilityResponse.setVinPattern( compat.getVinPattern() );
        vehicleCompatibilityResponse.setNotes( compat.getNotes() );

        return vehicleCompatibilityResponse;
    }

    @Override
    public PharmaExtensionResponse toPharmaResponse(PharmaExtension ext) {
        if ( ext == null ) {
            return null;
        }

        PharmaExtensionResponse pharmaExtensionResponse = new PharmaExtensionResponse();

        pharmaExtensionResponse.setId( ext.getId() );
        pharmaExtensionResponse.setDci( ext.getDci() );
        pharmaExtensionResponse.setDosage( ext.getDosage() );
        if ( ext.getGalenic() != null ) {
            pharmaExtensionResponse.setGalenic( ext.getGalenic().name() );
        }
        pharmaExtensionResponse.setTherapeuticClass( ext.getTherapeuticClass() );
        pharmaExtensionResponse.setAmmNumber( ext.getAmmNumber() );
        pharmaExtensionResponse.setRequiresPrescription( ext.isRequiresPrescription() );
        pharmaExtensionResponse.setGeneric( ext.isGeneric() );
        pharmaExtensionResponse.setPrincepsRef( ext.getPrincepsRef() );
        pharmaExtensionResponse.setLaboratoryName( ext.getLaboratoryName() );
        if ( ext.getStorageTemp() != null ) {
            pharmaExtensionResponse.setStorageTemp( ext.getStorageTemp().name() );
        }
        pharmaExtensionResponse.setShelfLifeDays( ext.getShelfLifeDays() );

        return pharmaExtensionResponse;
    }

    @Override
    public HardwareExtensionResponse toHardwareResponse(HardwareExtension ext) {
        if ( ext == null ) {
            return null;
        }

        HardwareExtensionResponse hardwareExtensionResponse = new HardwareExtensionResponse();

        hardwareExtensionResponse.setId( ext.getId() );
        if ( ext.getFamilyEnum() != null ) {
            hardwareExtensionResponse.setFamilyEnum( ext.getFamilyEnum().name() );
        }
        hardwareExtensionResponse.setMaterial( ext.getMaterial() );
        hardwareExtensionResponse.setDimensions( ext.getDimensions() );
        hardwareExtensionResponse.setNorm( ext.getNorm() );
        hardwareExtensionResponse.setConditioning( ext.getConditioning() );
        hardwareExtensionResponse.setConditioningQty( ext.getConditioningQty() );
        hardwareExtensionResponse.setProfessional( ext.isProfessional() );
        hardwareExtensionResponse.setColorOrFinish( ext.getColorOrFinish() );

        return hardwareExtensionResponse;
    }

    private UUID productCategoryId(Product product) {
        if ( product == null ) {
            return null;
        }
        Category category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        UUID id = category.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String productCategoryName(Product product) {
        if ( product == null ) {
            return null;
        }
        Category category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        String name = category.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String productCategoryColorHex(Product product) {
        if ( product == null ) {
            return null;
        }
        Category category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        String colorHex = category.getColorHex();
        if ( colorHex == null ) {
            return null;
        }
        return colorHex;
    }

    private String productCategoryIconName(Product product) {
        if ( product == null ) {
            return null;
        }
        Category category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        String iconName = category.getIconName();
        if ( iconName == null ) {
            return null;
        }
        return iconName;
    }

    private UUID productUnitId(Product product) {
        if ( product == null ) {
            return null;
        }
        Unit unit = product.getUnit();
        if ( unit == null ) {
            return null;
        }
        UUID id = unit.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String productUnitSymbol(Product product) {
        if ( product == null ) {
            return null;
        }
        Unit unit = product.getUnit();
        if ( unit == null ) {
            return null;
        }
        String symbol = unit.getSymbol();
        if ( symbol == null ) {
            return null;
        }
        return symbol;
    }

    protected List<VehicleCompatibilityResponse> vehicleCompatibilityListToVehicleCompatibilityResponseList(List<VehicleCompatibility> list) {
        if ( list == null ) {
            return null;
        }

        List<VehicleCompatibilityResponse> list1 = new ArrayList<VehicleCompatibilityResponse>( list.size() );
        for ( VehicleCompatibility vehicleCompatibility : list ) {
            list1.add( toCompatibilityResponse( vehicleCompatibility ) );
        }

        return list1;
    }
}
