package com.erp.platform.inventory.service;

import com.erp.platform.catalog.entity.Product;
import com.erp.platform.catalog.repository.ProductRepository;
import com.erp.platform.core.exception.ConflictException;
import com.erp.platform.core.exception.ResourceNotFoundException;
import com.erp.platform.core.security.JwtUserPrincipal;
import com.erp.platform.iam.entity.LocalUnit;
import com.erp.platform.iam.repository.LocalUnitRepository;
import com.erp.platform.inventory.dto.request.CreateInventoryRequest;
import com.erp.platform.inventory.dto.request.StockAdjustmentRequest;
import com.erp.platform.inventory.dto.request.UpdateInventoryLineRequest;
import com.erp.platform.inventory.dto.response.InventoryLineResponse;
import com.erp.platform.inventory.dto.response.InventoryResponse;
import com.erp.platform.inventory.entity.PhysicalInventory;
import com.erp.platform.inventory.entity.PhysicalInventoryLine;
import com.erp.platform.inventory.entity.StockEntry;
import com.erp.platform.inventory.repository.PhysicalInventoryLineRepository;
import com.erp.platform.inventory.repository.PhysicalInventoryRepository;
import com.erp.platform.inventory.repository.StockEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhysicalInventoryService {

    private final PhysicalInventoryRepository inventoryRepository;
    private final PhysicalInventoryLineRepository lineRepository;
    private final LocalUnitRepository localUnitRepository;
    private final ProductRepository productRepository;
    private final StockEntryRepository entryRepository;
    private final StockMovementService movementService;

    @Transactional
    public InventoryResponse createInventory(CreateInventoryRequest req, JwtUserPrincipal principal) {
        LocalUnit local = localUnitRepository.findByIdAndIsDeletedFalse(req.getLocalId())
                .orElseThrow(() -> new ResourceNotFoundException("LocalUnit", req.getLocalId().toString()));

        List<Product> products;
        if ("PARTIAL".equalsIgnoreCase(req.getType()) && req.getProductIds() != null && !req.getProductIds().isEmpty()) {
            products = productRepository.findAllByIdInAndIsDeletedFalse(req.getProductIds())
                    .stream().filter(Product::isTrackStock).collect(Collectors.toList());
        } else {
            products = productRepository.findAll().stream()
                    .filter(p -> !p.isDeleted() && p.isTrackStock())
                    .collect(Collectors.toList());
        }

        UUID userId = principal != null && principal.getUserId() != null ? UUID.fromString(principal.getUserId()) : null;

        PhysicalInventory inv = PhysicalInventory.builder()
                .localId(local.getId())
                .status("IN_PROGRESS")
                .type(req.getType() != null ? req.getType() : "PARTIAL")
                .startedAt(LocalDateTime.now())
                .createdBy(userId)
                .totalItems(products.size())
                .totalVarianceValue(BigDecimal.ZERO)
                .lines(new ArrayList<>())
                .build();
        inv.setCreatedAt(LocalDateTime.now());
        inv = inventoryRepository.save(inv);

        List<StockEntry> existingEntries = entryRepository.findByLocalIdAndIsDeletedFalse(local.getId());
        Map<UUID, StockEntry> entryMap = existingEntries.stream()
                .collect(Collectors.toMap(StockEntry::getProductId, e -> e));

        List<PhysicalInventoryLine> lines = new ArrayList<>();
        for (Product p : products) {
            StockEntry entry = entryMap.get(p.getId());
            BigDecimal expectedQty = entry != null && entry.getQuantity() != null ? entry.getQuantity() : BigDecimal.ZERO;

            PhysicalInventoryLine line = PhysicalInventoryLine.builder()
                    .inventory(inv)
                    .productId(p.getId())
                    .expectedQty(expectedQty)
                    .isCounted(false)
                    .build();
            line.setCreatedAt(LocalDateTime.now());
            lines.add(line);
        }

        lines = lineRepository.saveAll(lines);
        inv.setLines(lines);

        return toResponse(inv);
    }

    @Transactional
    public InventoryLineResponse updateLine(UUID inventoryId, UUID lineId, UpdateInventoryLineRequest req) {
        PhysicalInventoryLine line = lineRepository.findById(lineId)
                .filter(l -> !l.isDeleted() && l.getInventory().getId().equals(inventoryId))
                .orElseThrow(() -> new ResourceNotFoundException("PhysicalInventoryLine", lineId.toString()));

        if (!"IN_PROGRESS".equals(line.getInventory().getStatus())) {
            throw new ConflictException("L'inventaire n'est plus en cours", "status");
        }

        line.setCountedQty(req.getCountedQty());
        line.setVarianceQty(req.getCountedQty().subtract(line.getExpectedQty()));
        line.setCounted(true);
        line.setUpdatedAt(LocalDateTime.now());
        line = lineRepository.save(line);

        Product p = productRepository.findByIdAndIsDeletedFalse(line.getProductId()).orElse(null);
        return toLineResponse(line, p);
    }

    @Transactional
    public InventoryResponse completeInventory(UUID inventoryId, JwtUserPrincipal principal) {
        PhysicalInventory inv = inventoryRepository.findByIdAndIsDeletedFalse(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("PhysicalInventory", inventoryId.toString()));

        if (!"IN_PROGRESS".equals(inv.getStatus())) {
            throw new ConflictException("L'inventaire est déjà clôturé ou annulé", "status");
        }

        BigDecimal totalVarianceValue = BigDecimal.ZERO;
        for (PhysicalInventoryLine line : inv.getLines()) {
            if (line.isCounted() && line.getVarianceQty() != null && line.getVarianceQty().compareTo(BigDecimal.ZERO) != 0) {
                StockAdjustmentRequest adjReq = StockAdjustmentRequest.builder()
                        .productId(line.getProductId())
                        .localId(inv.getLocalId())
                        .newQuantity(line.getCountedQty())
                        .reason("Inventaire physique #" + inv.getId())
                        .build();

                movementService.adjust(adjReq, principal);

                Product p = productRepository.findByIdAndIsDeletedFalse(line.getProductId()).orElse(null);
                BigDecimal cost = p != null && p.getPurchasePriceHT() != null ? p.getPurchasePriceHT() : BigDecimal.ZERO;
                totalVarianceValue = totalVarianceValue.add(line.getVarianceQty().multiply(cost));
            }
        }

        inv.setStatus("COMPLETED");
        inv.setCompletedAt(LocalDateTime.now());
        inv.setTotalVarianceValue(totalVarianceValue);
        inv.setUpdatedAt(LocalDateTime.now());
        inv = inventoryRepository.save(inv);

        return toResponse(inv);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getByLocal(UUID localId) {
        List<PhysicalInventory> invs = localId != null
                ? inventoryRepository.findByLocalIdAndIsDeletedFalseOrderByStartedAtDesc(localId)
                : inventoryRepository.findByIsDeletedFalseOrderByStartedAtDesc();
        return invs.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InventoryResponse getById(UUID id) {
        PhysicalInventory inv = inventoryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PhysicalInventory", id.toString()));
        return toResponse(inv);
    }

    private InventoryResponse toResponse(PhysicalInventory inv) {
        LocalUnit local = localUnitRepository.findByIdAndIsDeletedFalse(inv.getLocalId()).orElse(null);

        Set<UUID> productIds = inv.getLines().stream().map(PhysicalInventoryLine::getProductId).collect(Collectors.toSet());
        Map<UUID, Product> productMap = productIds.isEmpty() ? Collections.emptyMap() :
                productRepository.findAllByIdInAndIsDeletedFalse(productIds).stream()
                        .collect(Collectors.toMap(Product::getId, p -> p));

        List<InventoryLineResponse> lineResponses = inv.getLines().stream()
                .map(l -> toLineResponse(l, productMap.get(l.getProductId())))
                .collect(Collectors.toList());

        return InventoryResponse.builder()
                .id(inv.getId())
                .localId(inv.getLocalId())
                .localName(local != null ? local.getName() : null)
                .status(inv.getStatus())
                .type(inv.getType())
                .startedAt(inv.getStartedAt())
                .completedAt(inv.getCompletedAt())
                .createdBy(inv.getCreatedBy())
                .totalItems(inv.getTotalItems())
                .totalVarianceValue(inv.getTotalVarianceValue())
                .lines(lineResponses)
                .build();
    }

    private InventoryLineResponse toLineResponse(PhysicalInventoryLine l, Product p) {
        return InventoryLineResponse.builder()
                .id(l.getId())
                .productId(l.getProductId())
                .productCode(p != null ? p.getRef() : null)
                .productName(p != null ? p.getName() : null)
                .expectedQty(l.getExpectedQty())
                .countedQty(l.getCountedQty())
                .varianceQty(l.getVarianceQty())
                .isCounted(l.isCounted())
                .build();
    }
}
