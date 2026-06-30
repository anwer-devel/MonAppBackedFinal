package com.erp.platform.inventory.service;

import com.erp.platform.catalog.entity.Product;
import com.erp.platform.catalog.repository.ProductRepository;
import com.erp.platform.core.audit.AuditService;
import com.erp.platform.core.common.PageResponse;
import com.erp.platform.core.exception.ConflictException;
import com.erp.platform.core.exception.ResourceNotFoundException;
import com.erp.platform.core.security.JwtUserPrincipal;
import com.erp.platform.iam.entity.LocalUnit;
import com.erp.platform.iam.repository.LocalUnitRepository;
import com.erp.platform.inventory.dto.request.CreateMovementRequest;
import com.erp.platform.inventory.dto.request.StockAdjustmentRequest;
import com.erp.platform.inventory.dto.request.StockTransferRequest;
import com.erp.platform.inventory.dto.response.StockMovementResponse;
import com.erp.platform.inventory.entity.StockEntry;
import com.erp.platform.inventory.entity.StockLot;
import com.erp.platform.inventory.entity.StockMovement;
import com.erp.platform.inventory.enums.MovementType;
import com.erp.platform.inventory.repository.StockEntryRepository;
import com.erp.platform.inventory.repository.StockLotRepository;
import com.erp.platform.inventory.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository movementRepository;
    private final StockEntryRepository entryRepository;
    private final StockLotRepository lotRepository;
    private final ProductRepository productRepository;
    private final LocalUnitRepository localUnitRepository;
    private final StockAlertService alertService;
    private final AuditService auditService;

    @Transactional
    public StockMovementResponse createMovement(CreateMovementRequest req, JwtUserPrincipal principal) {
        UUID userId = principal != null && principal.getUserId() != null ? UUID.fromString(principal.getUserId()) : null;
        String userName = principal != null ? principal.getEmail() : "SYSTEM";
        return createMovement(req, userId, userName);
    }

    @Transactional
    public StockMovementResponse createMovement(CreateMovementRequest req, UUID userId, String userName) {
        Product product = productRepository.findByIdAndIsDeletedFalse(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", req.getProductId().toString()));

        if (!product.isTrackStock()) {
            throw new ConflictException("product.not.trackable", "productId", "Ce produit n'est pas géré en stock");
        }

        LocalUnit local = localUnitRepository.findByIdAndIsDeletedFalse(req.getLocalId())
                .orElseThrow(() -> new ResourceNotFoundException("LocalUnit", req.getLocalId().toString()));

        StockEntry entry = entryRepository.findByProductIdAndLocalIdAndIsDeletedFalse(req.getProductId(), req.getLocalId())
                .orElseGet(() -> {
                    StockEntry newEntry = StockEntry.builder()
                            .productId(req.getProductId())
                            .localId(req.getLocalId())
                            .quantity(BigDecimal.ZERO)
                            .reservedQty(BigDecimal.ZERO)
                            .avgUnitCost(product.getPurchasePriceHT() != null ? product.getPurchasePriceHT() : BigDecimal.ZERO)
                            .build();
                    newEntry.setCreatedAt(LocalDateTime.now());
                    return newEntry;
                });

        boolean isIncoming = isIncomingMovement(req.getType());
        BigDecimal qtyDelta = isIncoming ? req.getQuantity() : req.getQuantity().negate();
        BigDecimal currentQty = entry.getQuantity() != null ? entry.getQuantity() : BigDecimal.ZERO;
        BigDecimal newQty = currentQty.add(qtyDelta);

        if (newQty.compareTo(BigDecimal.ZERO) < 0 && req.getType() != MovementType.ADJUSTMENT_OUT) {
            throw new ConflictException("insufficient.stock", "quantity",
                    "Stock insuffisant : " + currentQty + " disponible, " + req.getQuantity() + " demandé");
        }

        BigDecimal movementCost = req.getUnitCost() != null ? req.getUnitCost() : entry.getAvgUnitCost();

        if (isIncoming && movementCost != null && movementCost.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal currentTotalVal = currentQty.multiply(entry.getAvgUnitCost() != null ? entry.getAvgUnitCost() : BigDecimal.ZERO);
            BigDecimal incomingTotalVal = req.getQuantity().multiply(movementCost);
            BigDecimal totalQty = currentQty.add(req.getQuantity());
            if (totalQty.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal newAvgCost = currentTotalVal.add(incomingTotalVal).divide(totalQty, 4, RoundingMode.HALF_UP);
                entry.setAvgUnitCost(newAvgCost);
            }
        }

        entry.setQuantity(newQty);
        entry.setLastMovementAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        entryRepository.save(entry);

        UUID lotId = null;
        if (req.getLotNumber() != null && !req.getLotNumber().isBlank()) {
            lotId = handleLotMovement(req, isIncoming);
        }

        StockMovement movement = StockMovement.builder()
                .productId(req.getProductId())
                .localId(req.getLocalId())
                .type(req.getType())
                .quantity(req.getQuantity())
                .unitCost(movementCost)
                .balanceAfter(newQty)
                .reason(req.getReason())
                .referenceType(req.getReferenceType())
                .referenceId(req.getReferenceId())
                .lotId(lotId)
                .createdBy(userId)
                .createdByName(userName)
                .build();
        movement.setCreatedAt(LocalDateTime.now());
        movement = movementRepository.save(movement);

        alertService.checkAndUpdateAlerts(product, req.getLocalId(), newQty);

        if (userId != null) {
            auditService.log(userId, userName, "STOCK_MOVEMENT_CREATED", "StockMovement", movement.getId(), null, movement, null, null);
        }

        return toResponse(movement, product, local, req.getLotNumber());
    }

    @Transactional
    public List<StockMovementResponse> transfer(StockTransferRequest req, JwtUserPrincipal principal) {
        UUID userId = principal != null && principal.getUserId() != null ? UUID.fromString(principal.getUserId()) : null;
        String userName = principal != null ? principal.getEmail() : "SYSTEM";
        return transfer(req, userId, userName);
    }

    @Transactional
    public List<StockMovementResponse> transfer(StockTransferRequest req, UUID userId, String userName) {
        if (req.getSourceLocalId().equals(req.getTargetLocalId())) {
            throw new ConflictException("transfer.same.local", "targetLocalId", "Le local source et destination doivent être différents");
        }

        UUID transferGroupId = UUID.randomUUID();

        CreateMovementRequest outReq = CreateMovementRequest.builder()
                .productId(req.getProductId())
                .localId(req.getSourceLocalId())
                .type(MovementType.TRANSFER_OUT)
                .quantity(req.getQuantity())
                .reason(req.getReason() != null ? req.getReason() : "Transfert vers dépôt destination")
                .referenceType("TRANSFER")
                .referenceId(transferGroupId)
                .build();

        CreateMovementRequest inReq = CreateMovementRequest.builder()
                .productId(req.getProductId())
                .localId(req.getTargetLocalId())
                .type(MovementType.TRANSFER_IN)
                .quantity(req.getQuantity())
                .reason(req.getReason() != null ? req.getReason() : "Transfert depuis dépôt source")
                .referenceType("TRANSFER")
                .referenceId(transferGroupId)
                .build();

        StockMovementResponse outResp = createMovement(outReq, userId, userName);
        StockMovementResponse inResp = createMovement(inReq, userId, userName);

        return List.of(outResp, inResp);
    }

    @Transactional
    public StockMovementResponse adjust(StockAdjustmentRequest req, JwtUserPrincipal principal) {
        UUID userId = principal != null && principal.getUserId() != null ? UUID.fromString(principal.getUserId()) : null;
        String userName = principal != null ? principal.getEmail() : "SYSTEM";
        return adjust(req, userId, userName);
    }

    @Transactional
    public StockMovementResponse adjust(StockAdjustmentRequest req, UUID userId, String userName) {
        StockEntry entry = entryRepository.findByProductIdAndLocalIdAndIsDeletedFalse(req.getProductId(), req.getLocalId())
                .orElse(null);

        BigDecimal currentQty = entry != null && entry.getQuantity() != null ? entry.getQuantity() : BigDecimal.ZERO;
        BigDecimal diff = req.getNewQuantity().subtract(currentQty);

        if (diff.compareTo(BigDecimal.ZERO) == 0) {
            throw new ConflictException("adjustment.no.change", "newQuantity", "Aucun écart détecté, ajustement non nécessaire");
        }

        MovementType type = diff.compareTo(BigDecimal.ZERO) > 0 ? MovementType.ADJUSTMENT_IN : MovementType.ADJUSTMENT_OUT;
        CreateMovementRequest mvReq = CreateMovementRequest.builder()
                .productId(req.getProductId())
                .localId(req.getLocalId())
                .type(type)
                .quantity(diff.abs())
                .reason(req.getReason())
                .referenceType("ADJUSTMENT")
                .build();

        return createMovement(mvReq, userId, userName);
    }

    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> search(UUID productId, UUID localId, MovementType type,
                                                      LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable) {
        Page<StockMovement> page = movementRepository.findWithFilters(productId, localId, type, dateFrom, dateTo, pageable);

        Set<UUID> productIds = page.getContent().stream().map(StockMovement::getProductId).collect(Collectors.toSet());
        Set<UUID> localIds = page.getContent().stream().map(StockMovement::getLocalId).collect(Collectors.toSet());
        Set<UUID> lotIds = page.getContent().stream().map(StockMovement::getLotId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<UUID, Product> productMap = productIds.isEmpty() ? Collections.emptyMap() :
                productRepository.findAllByIdInAndIsDeletedFalse(productIds).stream()
                        .collect(Collectors.toMap(Product::getId, p -> p));

        Map<UUID, LocalUnit> localMap = localIds.isEmpty() ? Collections.emptyMap() :
                localUnitRepository.findAllByIdInAndIsDeletedFalse(localIds).stream()
                        .collect(Collectors.toMap(LocalUnit::getId, l -> l));

        Map<UUID, String> lotMap = lotIds.isEmpty() ? Collections.emptyMap() :
                lotRepository.findAllById(lotIds).stream()
                        .collect(Collectors.toMap(StockLot::getId, StockLot::getLotNumber));

        List<StockMovementResponse> content = page.getContent().stream()
                .map(m -> {
                    Product p = productMap.get(m.getProductId());
                    LocalUnit l = localMap.get(m.getLocalId());
                    String lotNum = m.getLotId() != null ? lotMap.get(m.getLotId()) : null;
                    return toResponse(m, p, l, lotNum);
                })
                .collect(Collectors.toList());

        return PageResponse.of(content, page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }

    private boolean isIncomingMovement(MovementType type) {
        return type == MovementType.IN || type == MovementType.TRANSFER_IN ||
               type == MovementType.RETURN_CUSTOMER || type == MovementType.ADJUSTMENT_IN;
    }

    private UUID handleLotMovement(CreateMovementRequest req, boolean isIncoming) {
        StockLot lot = lotRepository.findByProductIdAndLocalIdAndLotNumberAndIsDeletedFalse(
                req.getProductId(), req.getLocalId(), req.getLotNumber())
                .orElseGet(() -> {
                    StockLot l = StockLot.builder()
                            .productId(req.getProductId())
                            .localId(req.getLocalId())
                            .lotNumber(req.getLotNumber())
                            .quantity(BigDecimal.ZERO)
                            .build();
                    l.setCreatedAt(LocalDateTime.now());
                    return l;
                });

        BigDecimal delta = isIncoming ? req.getQuantity() : req.getQuantity().negate();
        BigDecimal newQty = lot.getQuantity().add(delta);
        if (newQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new ConflictException("insufficient.lot.stock", "lotNumber", "Stock insuffisant pour le lot " + req.getLotNumber());
        }
        lot.setQuantity(newQty);
        if (isIncoming && req.getUnitCost() != null) {
            lot.setUnitCost(req.getUnitCost());
        }
        lot.setUpdatedAt(LocalDateTime.now());
        lot = lotRepository.save(lot);
        return lot.getId();
    }

    private StockMovementResponse toResponse(StockMovement m, Product p, LocalUnit l, String lotNumber) {
        return StockMovementResponse.builder()
                .id(m.getId())
                .productId(m.getProductId())
                .productRef(p != null ? p.getRef() : null)
                .productCode(p != null ? p.getRef() : null)
                .productName(p != null ? p.getName() : null)
                .localId(m.getLocalId())
                .localName(l != null ? l.getName() : null)
                .type(m.getType())
                .typeLabel(movementTypeLabel(m.getType()))
                .quantity(m.getQuantity())
                .unitCost(m.getUnitCost())
                .balanceAfter(m.getBalanceAfter())
                .reason(m.getReason())
                .referenceType(m.getReferenceType())
                .referenceId(m.getReferenceId())
                .targetLocalId(m.getTargetLocalId())
                .lotNumber(lotNumber)
                .createdBy(m.getCreatedBy())
                .createdByName(m.getCreatedByName())
                .createdAt(m.getCreatedAt())
                .build();
    }

    private String movementTypeLabel(MovementType type) {
        if (type == null) return "";
        return switch (type) {
            case IN -> "Entrée";
            case OUT -> "Sortie";
            case TRANSFER_OUT -> "Transfert sortant";
            case TRANSFER_IN -> "Transfert entrant";
            case RETURN_CUSTOMER -> "Retour client";
            case RETURN_SUPPLIER -> "Retour fournisseur";
            case ADJUSTMENT_IN -> "Ajustement +";
            case ADJUSTMENT_OUT -> "Ajustement -";
        };
    }
}
