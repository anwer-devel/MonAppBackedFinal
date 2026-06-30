package com.erp.platform.inventory.service;

import com.erp.platform.catalog.entity.Product;
import com.erp.platform.catalog.repository.ProductRepository;
import com.erp.platform.core.exception.ConflictException;
import com.erp.platform.core.exception.ResourceNotFoundException;
import com.erp.platform.iam.entity.LocalUnit;
import com.erp.platform.iam.repository.LocalUnitRepository;
import com.erp.platform.inventory.dto.request.CreateLotRequest;
import com.erp.platform.inventory.dto.response.StockLotResponse;
import com.erp.platform.inventory.entity.StockLot;
import com.erp.platform.inventory.repository.StockLotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockLotService {

    private final StockLotRepository lotRepository;
    private final ProductRepository productRepository;
    private final LocalUnitRepository localUnitRepository;

    @Transactional
    public StockLotResponse createLot(CreateLotRequest req) {
        Product product = productRepository.findByIdAndIsDeletedFalse(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", req.getProductId().toString()));

        if (!product.isTrackStock()) {
            throw new ConflictException("product.not.trackable", "productId", "Ce produit n'est pas géré en stock");
        }

        LocalUnit local = localUnitRepository.findByIdAndIsDeletedFalse(req.getLocalId())
                .orElseThrow(() -> new ResourceNotFoundException("LocalUnit", req.getLocalId().toString()));

        Optional<StockLot> existing = lotRepository.findByProductIdAndLocalIdAndLotNumberAndIsDeletedFalse(
                req.getProductId(), req.getLocalId(), req.getLotNumber());
        if (existing.isPresent()) {
            throw new ConflictException("Un lot avec ce numéro existe déjà dans ce local", "lotNumber");
        }

        StockLot lot = StockLot.builder()
                .productId(req.getProductId())
                .localId(req.getLocalId())
                .lotNumber(req.getLotNumber())
                .quantity(req.getQuantity())
                .expiryDate(req.getExpiryDate())
                .manufactureDate(req.getManufactureDate())
                .supplierRef(req.getSupplierRef())
                .unitCost(req.getUnitCost())
                .build();
        lot.setCreatedAt(LocalDateTime.now());
        lot = lotRepository.save(lot);

        return toResponse(lot, product, local);
    }

    @Transactional(readOnly = true)
    public List<StockLotResponse> getByProductAndLocal(UUID productId, UUID localId) {
        List<StockLot> lots = lotRepository.findByProductIdAndLocalIdAndIsDeletedFalseOrderByExpiryDateAsc(productId, localId);
        return mapListToResponses(lots);
    }

    @Transactional(readOnly = true)
    public List<StockLotResponse> getExpiringSoon(int days) {
        LocalDate beforeDate = LocalDate.now().plusDays(days);
        List<StockLot> lots = lotRepository.findExpiringSoon(beforeDate);
        return mapListToResponses(lots);
    }

    private List<StockLotResponse> mapListToResponses(List<StockLot> lots) {
        Set<UUID> productIds = lots.stream().map(StockLot::getProductId).collect(Collectors.toSet());
        Set<UUID> localIds = lots.stream().map(StockLot::getLocalId).collect(Collectors.toSet());

        Map<UUID, Product> productMap = productIds.isEmpty() ? Collections.emptyMap() :
                productRepository.findAllByIdInAndIsDeletedFalse(productIds).stream()
                        .collect(Collectors.toMap(Product::getId, p -> p));

        Map<UUID, LocalUnit> localMap = localIds.isEmpty() ? Collections.emptyMap() :
                localUnitRepository.findAllByIdInAndIsDeletedFalse(localIds).stream()
                        .collect(Collectors.toMap(LocalUnit::getId, l -> l));

        return lots.stream()
                .map(l -> toResponse(l, productMap.get(l.getProductId()), localMap.get(l.getLocalId())))
                .collect(Collectors.toList());
    }

    private StockLotResponse toResponse(StockLot l, Product p, LocalUnit u) {
        Integer daysUntilExpiry = null;
        String expiryStatus = "OK";

        if (l.getExpiryDate() != null) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), l.getExpiryDate());
            daysUntilExpiry = (int) days;
            expiryStatus = days < 0 ? "EXPIRED" : days <= 30 ? "SOON" : "OK";
        }

        return StockLotResponse.builder()
                .id(l.getId())
                .productId(l.getProductId())
                .productRef(p != null ? p.getRef() : null)
                .productCode(p != null ? p.getRef() : null)
                .productName(p != null ? p.getName() : null)
                .localId(l.getLocalId())
                .localName(u != null ? u.getName() : null)
                .lotNumber(l.getLotNumber())
                .quantity(l.getQuantity())
                .expiryDate(l.getExpiryDate())
                .manufactureDate(l.getManufactureDate())
                .supplierRef(l.getSupplierRef())
                .unitCost(l.getUnitCost())
                .daysUntilExpiry(daysUntilExpiry)
                .expiryStatus(expiryStatus)
                .build();
    }
}
