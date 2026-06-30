package com.erp.platform.inventory.service;

import com.erp.platform.catalog.entity.Product;
import com.erp.platform.catalog.repository.ProductRepository;
import com.erp.platform.core.common.PageResponse;
import com.erp.platform.core.exception.ResourceNotFoundException;
import com.erp.platform.iam.entity.LocalUnit;
import com.erp.platform.iam.repository.LocalUnitRepository;
import com.erp.platform.inventory.dto.response.MultiLocationStockResponse;
import com.erp.platform.inventory.dto.response.StockEntryResponse;
import com.erp.platform.inventory.entity.StockEntry;
import com.erp.platform.inventory.repository.StockEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockEntryService {

    private final StockEntryRepository entryRepository;
    private final ProductRepository productRepository;
    private final LocalUnitRepository localUnitRepository;

    @Transactional(readOnly = true)
    public StockEntryResponse getByProductAndLocal(UUID productId, UUID localId) {
        Product p = productRepository.findByIdAndIsDeletedFalse(productId).orElse(null);
        LocalUnit l = localUnitRepository.findByIdAndIsDeletedFalse(localId).orElse(null);

        return entryRepository.findByProductIdAndLocalIdAndIsDeletedFalse(productId, localId)
                .map(e -> toResponse(e, p, l))
                .orElseGet(() -> StockEntryResponse.builder()
                        .productId(productId)
                        .productRef(p != null ? p.getRef() : null)
                        .productCode(p != null ? p.getRef() : null)
                        .productName(p != null ? p.getName() : null)
                        .localId(localId)
                        .localName(l != null ? l.getName() : null)
                        .localCode(l != null ? l.getCode() : null)
                        .quantity(BigDecimal.ZERO)
                        .reservedQty(BigDecimal.ZERO)
                        .availableQty(BigDecimal.ZERO)
                        .avgUnitCost(p != null && p.getPurchasePriceHT() != null ? p.getPurchasePriceHT() : BigDecimal.ZERO)
                        .totalValue(BigDecimal.ZERO)
                        .stockStatus("OUT")
                        .build());
    }

    @Transactional(readOnly = true)
    public List<StockEntryResponse> getByProduct(UUID productId) {
        List<StockEntry> entries = entryRepository.findByProductIdAndIsDeletedFalse(productId);
        return mapListToResponses(entries);
    }

    @Transactional(readOnly = true)
    public List<StockEntryResponse> getByLocal(UUID localId) {
        List<StockEntry> entries = entryRepository.findByLocalIdAndIsDeletedFalse(localId);
        return mapListToResponses(entries);
    }

    @Transactional(readOnly = true)
    public PageResponse<MultiLocationStockResponse> getMultiLocationView(String q, UUID categoryId, String status, int page, int size) {
        List<Product> products = productRepository.findAll().stream()
                .filter(p -> !p.isDeleted() && p.isTrackStock())
                .filter(p -> q == null || q.isBlank() || p.getName().toLowerCase().contains(q.toLowerCase()) || (p.getRef() != null && p.getRef().toLowerCase().contains(q.toLowerCase())))
                .filter(p -> categoryId == null || (p.getCategory() != null && categoryId.equals(p.getCategory().getId())))
                .collect(Collectors.toList());

        List<LocalUnit> locals = localUnitRepository.findAll().stream()
                .filter(l -> !l.isDeleted())
                .collect(Collectors.toList());

        Map<UUID, String> localNames = locals.stream()
                .collect(Collectors.toMap(LocalUnit::getId, LocalUnit::getName));

        Set<UUID> productIds = products.stream().map(Product::getId).collect(Collectors.toSet());
        List<StockEntry> entries = productIds.isEmpty() ? Collections.emptyList() :
                entryRepository.findByProductIdInAndIsDeletedFalse(productIds);

        Map<UUID, List<StockEntry>> entriesByProduct = entries.stream()
                .collect(Collectors.groupingBy(StockEntry::getProductId));

        List<MultiLocationStockResponse> allResponses = new ArrayList<>();

        for (Product p : products) {
            List<StockEntry> prodEntries = entriesByProduct.getOrDefault(p.getId(), Collections.emptyList());
            Map<String, BigDecimal> quantityByLocal = new LinkedHashMap<>();
            BigDecimal totalQty = BigDecimal.ZERO;
            BigDecimal totalAvail = BigDecimal.ZERO;
            List<MultiLocationStockResponse.LocalStockDetail> locDetails = new ArrayList<>();

            for (LocalUnit l : locals) {
                StockEntry match = prodEntries.stream()
                        .filter(e -> e.getLocalId().equals(l.getId()))
                        .findFirst().orElse(null);

                BigDecimal qty = match != null && match.getQuantity() != null ? match.getQuantity() : BigDecimal.ZERO;
                BigDecimal avail = match != null ? match.getAvailableQty() : BigDecimal.ZERO;
                quantityByLocal.put(l.getName(), qty);
                totalQty = totalQty.add(qty);
                totalAvail = totalAvail.add(avail);
                locDetails.add(MultiLocationStockResponse.LocalStockDetail.builder()
                        .localId(l.getId())
                        .localName(l.getName())
                        .quantity(qty)
                        .availableQty(avail)
                        .build());
            }

            String stStatus = calculateStockStatus(totalQty, p);
            if (status != null && !status.isBlank() && !status.equalsIgnoreCase(stStatus)) {
                continue;
            }

            BigDecimal valuation = totalQty.multiply(p.getPurchasePriceHT() != null ? p.getPurchasePriceHT() : BigDecimal.ZERO);

            allResponses.add(MultiLocationStockResponse.builder()
                    .productId(p.getId())
                    .productRef(p.getRef())
                    .productCode(p.getRef())
                    .productName(p.getName())
                    .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                    .quantityByLocal(quantityByLocal)
                    .totalQuantity(totalQty)
                    .totalAvailable(totalAvail)
                    .stockStatus(stStatus)
                    .valuation(valuation)
                    .salePriceHT(p.getSalePriceHT())
                    .locations(locDetails)
                    .build());
        }

        int fromIndex = Math.min(page * size, allResponses.size());
        int toIndex = Math.min(fromIndex + size, allResponses.size());
        List<MultiLocationStockResponse> pageContent = allResponses.subList(fromIndex, toIndex);

        int totalPages = (int) Math.ceil((double) allResponses.size() / size);

        return PageResponse.of(pageContent, allResponses.size(), totalPages, page, size);
    }

    private String calculateStockStatus(BigDecimal qty, Product p) {
        if (qty.compareTo(BigDecimal.ZERO) <= 0) return "OUT";
        if (qty.compareTo(BigDecimal.valueOf(p.getMinStockLevel())) <= 0) return "LOW";
        return "OK";
    }

    private List<StockEntryResponse> mapListToResponses(List<StockEntry> entries) {
        Set<UUID> productIds = entries.stream().map(StockEntry::getProductId).collect(Collectors.toSet());
        Set<UUID> localIds = entries.stream().map(StockEntry::getLocalId).collect(Collectors.toSet());

        Map<UUID, Product> productMap = productIds.isEmpty() ? Collections.emptyMap() :
                productRepository.findAllByIdInAndIsDeletedFalse(productIds).stream()
                        .collect(Collectors.toMap(Product::getId, p -> p));

        Map<UUID, LocalUnit> localMap = localIds.isEmpty() ? Collections.emptyMap() :
                localUnitRepository.findAllByIdInAndIsDeletedFalse(localIds).stream()
                        .collect(Collectors.toMap(LocalUnit::getId, l -> l));

        return entries.stream()
                .map(e -> toResponse(e, productMap.get(e.getProductId()), localMap.get(e.getLocalId())))
                .collect(Collectors.toList());
    }

    private StockEntryResponse toResponse(StockEntry e, Product p, LocalUnit l) {
        BigDecimal q = e.getQuantity() != null ? e.getQuantity() : BigDecimal.ZERO;
        BigDecimal cost = e.getAvgUnitCost() != null ? e.getAvgUnitCost() : BigDecimal.ZERO;

        return StockEntryResponse.builder()
                .id(e.getId())
                .productId(e.getProductId())
                .productRef(p != null ? p.getRef() : null)
                .productCode(p != null ? p.getRef() : null)
                .productName(p != null ? p.getName() : null)
                .localId(e.getLocalId())
                .localName(l != null ? l.getName() : null)
                .localCode(l != null ? l.getCode() : null)
                .quantity(q)
                .reservedQty(e.getReservedQty() != null ? e.getReservedQty() : BigDecimal.ZERO)
                .availableQty(e.getAvailableQty())
                .avgUnitCost(cost)
                .totalValue(q.multiply(cost))
                .stockStatus(p != null ? calculateStockStatus(q, p) : "OK")
                .lastMovementAt(e.getLastMovementAt())
                .build();
    }
}
