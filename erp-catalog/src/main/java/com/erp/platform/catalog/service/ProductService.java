package com.erp.platform.catalog.service;

import com.erp.platform.catalog.dto.request.*;
import com.erp.platform.catalog.dto.response.*;
import com.erp.platform.catalog.entity.*;
import com.erp.platform.catalog.mapper.ProductMapper;
import com.erp.platform.catalog.repository.*;
import com.erp.platform.core.audit.AuditService;
import com.erp.platform.core.common.PageResponse;
import com.erp.platform.core.exception.ConflictException;
import com.erp.platform.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UnitRepository unitRepository;
    private final ProductMapper productMapper;
    private final AuditService auditService;

    public PageResponse<ProductSummaryResponse> search(
            UUID categoryId, String sectorType,
            Boolean isActive, Boolean isFavorite,
            String q, int page, int size, String sort) {

        String qPattern = (q != null && !q.trim().isEmpty()) ? "%" + q.trim().toLowerCase() + "%" : null;

        Sort.Direction dir = Sort.Direction.ASC;
        String prop = "name";
        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            prop = parts[0];
            if ("desc".equalsIgnoreCase(parts[1])) {
                dir = Sort.Direction.DESC;
            }
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, prop));
        Page<Product> productPage = productRepository.searchProducts(categoryId, sectorType, isActive, isFavorite, (q != null && !q.trim().isEmpty()) ? q.trim() : null, qPattern, pageable);

        List<ProductSummaryResponse> content = productPage.getContent().stream()
                .map(productMapper::toSummaryResponse)
                .collect(Collectors.toList());

        return PageResponse.of(content, productPage.getTotalElements(), productPage.getTotalPages(), productPage.getNumber(), productPage.getSize());
    }

    public Optional<ProductSummaryResponse> scanLookup(String code) {
        return productRepository.findByBarcodeOrRef(code)
                .map(productMapper::toSummaryResponse);
    }

    public List<ProductSummaryResponse> getFavorites() {
        return productRepository.findFavorites().stream()
                .map(productMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getById(UUID id) {
        Product product = productRepository.findByIdWithExtensions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return productMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse create(CreateProductRequest req) {
        String normalizedRef = req.getRef().trim().toUpperCase();
        req.setRef(normalizedRef);

        if (productRepository.findByRefIgnoreCaseAndIsDeletedFalse(normalizedRef).isPresent()) {
            throw new ConflictException("La référence produit '" + normalizedRef + "' est déjà utilisée", "ref");
        }
        if (req.getBarcode() != null && !req.getBarcode().trim().isEmpty()) {
            if (productRepository.findByBarcodeAndIsDeletedFalse(req.getBarcode().trim()).isPresent()) {
                throw new ConflictException("Le code-barres est déjà utilisé", "barcode");
            }
        }

        Category category = null;
        if (req.getCategoryId() != null) {
            category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", req.getCategoryId()));
        }

        Unit unit = null;
        if (req.getUnitId() != null) {
            unit = unitRepository.findById(req.getUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", req.getUnitId()));
        }

        BigDecimal marginRate = null;
        if (req.getPurchasePriceHT() != null && req.getPurchasePriceHT().compareTo(BigDecimal.ZERO) > 0) {
            marginRate = req.getSalePriceHT().subtract(req.getPurchasePriceHT())
                    .divide(req.getPurchasePriceHT(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
        }

        Product product = Product.builder()
                .ref(normalizedRef)
                .barcode(req.getBarcode())
                .name(req.getName())
                .description(req.getDescription())
                .shortDescription(req.getShortDescription())
                .category(category)
                .unit(unit)
                .sectorType(req.getSectorType())
                .purchasePriceHT(req.getPurchasePriceHT() != null ? req.getPurchasePriceHT() : BigDecimal.ZERO)
                .salePriceHT(req.getSalePriceHT())
                .taxRate(req.getTaxRate() != null ? req.getTaxRate() : new BigDecimal("19.0"))
                .marginRate(marginRate)
                .minStockLevel(req.getMinStockLevel())
                .safetyStockLevel(req.getSafetyStockLevel())
                .maxStockLevel(req.getMaxStockLevel() > 0 ? req.getMaxStockLevel() : 9999)
                .trackStock(req.isTrackStock())
                .isActive(req.isActive())
                .isFavorite(req.isFavorite())
                .imageUrls(req.getImageUrls() != null ? req.getImageUrls().toArray(new String[0]) : null)
                .attributes(req.getAttributes() != null ? req.getAttributes() : new java.util.HashMap<>())
                .build();

        if (req.getAutoPartExt() != null) {
            AutoPartExtension ape = AutoPartExtension.builder()
                    .product(product)
                    .oemRef(req.getAutoPartExt().getOemRef())
                    .aftermarketRef(req.getAutoPartExt().getAftermarketRef())
                    .brand(req.getAutoPartExt().getBrand())
                    .familyEnum(req.getAutoPartExt().getFamilyEnum())
                    .technicalNote(req.getAutoPartExt().getTechnicalNote())
                    .dataSheetUrl(req.getAutoPartExt().getDataSheetUrl())
                    .isOemEquivalent(req.getAutoPartExt().isOemEquivalent())
                    .build();

            if (req.getAutoPartExt().getCompatibilities() != null) {
                for (VehicleCompatibilityRequest vcr : req.getAutoPartExt().getCompatibilities()) {
                    VehicleCompatibility vc = VehicleCompatibility.builder()
                            .autoPart(ape)
                            .vehicleMake(vcr.getVehicleMake())
                            .vehicleModel(vcr.getVehicleModel())
                            .vehicleVariant(vcr.getVehicleVariant())
                            .yearFrom(vcr.getYearFrom())
                            .yearTo(vcr.getYearTo())
                            .engineType(vcr.getEngineType())
                            .vinPattern(vcr.getVinPattern())
                            .notes(vcr.getNotes())
                            .build();
                    ape.getCompatibilities().add(vc);
                }
            }
            product.setAutoPartExt(ape);
        }

        if (req.getPharmaExt() != null) {
            PharmaExtension pe = PharmaExtension.builder()
                    .product(product)
                    .dci(req.getPharmaExt().getDci())
                    .dosage(req.getPharmaExt().getDosage())
                    .galenic(req.getPharmaExt().getGalenic())
                    .therapeuticClass(req.getPharmaExt().getTherapeuticClass())
                    .ammNumber(req.getPharmaExt().getAmmNumber())
                    .requiresPrescription(req.getPharmaExt().isRequiresPrescription())
                    .isGeneric(req.getPharmaExt().isGeneric())
                    .princepsRef(req.getPharmaExt().getPrincepsRef())
                    .laboratoryName(req.getPharmaExt().getLaboratoryName())
                    .storageTemp(req.getPharmaExt().getStorageTemp() != null ? req.getPharmaExt().getStorageTemp() : com.erp.platform.catalog.enums.StorageTemp.AMBIENT)
                    .shelfLifeDays(req.getPharmaExt().getShelfLifeDays())
                    .build();
            product.setPharmaExt(pe);
        }

        if (req.getHardwareExt() != null) {
            HardwareExtension he = HardwareExtension.builder()
                    .product(product)
                    .familyEnum(req.getHardwareExt().getFamilyEnum())
                    .material(req.getHardwareExt().getMaterial())
                    .dimensions(req.getHardwareExt().getDimensions())
                    .norm(req.getHardwareExt().getNorm())
                    .conditioning(req.getHardwareExt().getConditioning())
                    .conditioningQty(req.getHardwareExt().getConditioningQty() > 0 ? req.getHardwareExt().getConditioningQty() : 1)
                    .isProfessional(req.getHardwareExt().isProfessional())
                    .colorOrFinish(req.getHardwareExt().getColorOrFinish())
                    .build();
            product.setHardwareExt(he);
        }

        Product saved = productRepository.save(product);
        auditService.log(null, null, "PRODUCT_CREATED", "Product", saved.getId(), null, saved.getRef(), null, null);
        return productMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse update(UUID id, UpdateProductRequest req) {
        Product product = productRepository.findByIdWithExtensions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (req.getRef() != null) {
            String normalizedRef = req.getRef().trim().toUpperCase();
            if (!product.getRef().equalsIgnoreCase(normalizedRef)) {
                if (productRepository.findByRefIgnoreCaseAndIsDeletedFalse(normalizedRef).isPresent()) {
                    throw new ConflictException("La référence produit '" + normalizedRef + "' est déjà utilisée", "ref");
                }
            }
            product.setRef(normalizedRef);
        }

        if (req.getBarcode() != null) {
            if (product.getBarcode() == null || !product.getBarcode().equalsIgnoreCase(req.getBarcode().trim())) {
                if (productRepository.findByBarcodeAndIsDeletedFalse(req.getBarcode().trim()).isPresent()) {
                    throw new ConflictException("Le code-barres est déjà utilisé", "barcode");
                }
            }
            product.setBarcode(req.getBarcode().trim());
        }

        if (req.getName() != null) product.setName(req.getName());
        if (req.getDescription() != null) product.setDescription(req.getDescription());
        if (req.getShortDescription() != null) product.setShortDescription(req.getShortDescription());
        if (req.getSectorType() != null) product.setSectorType(req.getSectorType());
        if (req.getPurchasePriceHT() != null) product.setPurchasePriceHT(req.getPurchasePriceHT());
        if (req.getSalePriceHT() != null) product.setSalePriceHT(req.getSalePriceHT());
        if (req.getTaxRate() != null) product.setTaxRate(req.getTaxRate());
        if (req.getMinStockLevel() != null) product.setMinStockLevel(req.getMinStockLevel());
        if (req.getSafetyStockLevel() != null) product.setSafetyStockLevel(req.getSafetyStockLevel());
        if (req.getMaxStockLevel() != null) product.setMaxStockLevel(req.getMaxStockLevel());
        if (req.getTrackStock() != null) product.setTrackStock(req.getTrackStock());
        if (req.getIsActive() != null) product.setActive(req.getIsActive());
        if (req.getIsFavorite() != null) product.setFavorite(req.getIsFavorite());
        if (req.getImageUrls() != null) product.setImageUrls(req.getImageUrls().toArray(new String[0]));
        if (req.getAttributes() != null) product.setAttributes(req.getAttributes());

        if (req.getCategoryId() != null) {
            Category cat = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", req.getCategoryId()));
            product.setCategory(cat);
        }
        if (req.getUnitId() != null) {
            Unit u = unitRepository.findById(req.getUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", req.getUnitId()));
            product.setUnit(u);
        }

        if (product.getPurchasePriceHT() != null && product.getPurchasePriceHT().compareTo(BigDecimal.ZERO) > 0 && product.getSalePriceHT() != null) {
            BigDecimal marginRate = product.getSalePriceHT().subtract(product.getPurchasePriceHT())
                    .divide(product.getPurchasePriceHT(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
            product.setMarginRate(marginRate);
        }

        if (req.getAutoPartExt() != null) {
            if (product.getAutoPartExt() == null) {
                AutoPartExtension ape = AutoPartExtension.builder().product(product).build();
                product.setAutoPartExt(ape);
            }
            product.getAutoPartExt().setOemRef(req.getAutoPartExt().getOemRef());
            product.getAutoPartExt().setAftermarketRef(req.getAutoPartExt().getAftermarketRef());
            product.getAutoPartExt().setBrand(req.getAutoPartExt().getBrand());
            product.getAutoPartExt().setFamilyEnum(req.getAutoPartExt().getFamilyEnum());
            product.getAutoPartExt().setTechnicalNote(req.getAutoPartExt().getTechnicalNote());
            product.getAutoPartExt().setDataSheetUrl(req.getAutoPartExt().getDataSheetUrl());
            product.getAutoPartExt().setOemEquivalent(req.getAutoPartExt().isOemEquivalent());
        }

        if (req.getPharmaExt() != null) {
            if (product.getPharmaExt() == null) {
                PharmaExtension pe = PharmaExtension.builder().product(product).build();
                product.setPharmaExt(pe);
            }
            product.getPharmaExt().setDci(req.getPharmaExt().getDci());
            product.getPharmaExt().setDosage(req.getPharmaExt().getDosage());
            product.getPharmaExt().setGalenic(req.getPharmaExt().getGalenic());
            product.getPharmaExt().setTherapeuticClass(req.getPharmaExt().getTherapeuticClass());
            product.getPharmaExt().setAmmNumber(req.getPharmaExt().getAmmNumber());
            product.getPharmaExt().setRequiresPrescription(req.getPharmaExt().isRequiresPrescription());
            product.getPharmaExt().setGeneric(req.getPharmaExt().isGeneric());
            product.getPharmaExt().setPrincepsRef(req.getPharmaExt().getPrincepsRef());
            product.getPharmaExt().setLaboratoryName(req.getPharmaExt().getLaboratoryName());
            if (req.getPharmaExt().getStorageTemp() != null) product.getPharmaExt().setStorageTemp(req.getPharmaExt().getStorageTemp());
            product.getPharmaExt().setShelfLifeDays(req.getPharmaExt().getShelfLifeDays());
        }

        if (req.getHardwareExt() != null) {
            if (product.getHardwareExt() == null) {
                HardwareExtension he = HardwareExtension.builder().product(product).build();
                product.setHardwareExt(he);
            }
            product.getHardwareExt().setFamilyEnum(req.getHardwareExt().getFamilyEnum());
            product.getHardwareExt().setMaterial(req.getHardwareExt().getMaterial());
            product.getHardwareExt().setDimensions(req.getHardwareExt().getDimensions());
            product.getHardwareExt().setNorm(req.getHardwareExt().getNorm());
            product.getHardwareExt().setConditioning(req.getHardwareExt().getConditioning());
            if (req.getHardwareExt().getConditioningQty() > 0) product.getHardwareExt().setConditioningQty(req.getHardwareExt().getConditioningQty());
            product.getHardwareExt().setProfessional(req.getHardwareExt().isProfessional());
            product.getHardwareExt().setColorOrFinish(req.getHardwareExt().getColorOrFinish());
        }

        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Transactional
    public void toggleFavorite(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setFavorite(!product.isFavorite());
        productRepository.save(product);
    }

    @Transactional
    public void toggleActive(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setActive(!product.isActive());
        productRepository.save(product);
    }

    @Transactional
    public void delete(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setDeleted(true);
        productRepository.save(product);
    }
}
