package com.erp.platform.inventory.service;

import com.erp.platform.catalog.entity.Product;
import com.erp.platform.catalog.repository.ProductRepository;
import com.erp.platform.core.exception.ResourceNotFoundException;
import com.erp.platform.iam.entity.LocalUnit;
import com.erp.platform.iam.repository.LocalUnitRepository;
import com.erp.platform.inventory.dto.response.StockAlertResponse;
import com.erp.platform.inventory.entity.StockAlert;
import com.erp.platform.inventory.entity.StockLot;
import com.erp.platform.inventory.enums.AlertSeverity;
import com.erp.platform.inventory.repository.StockAlertRepository;
import com.erp.platform.inventory.repository.StockLotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockAlertService {

    private final StockAlertRepository alertRepository;
    private final StockLotRepository lotRepository;
    private final ProductRepository productRepository;
    private final LocalUnitRepository localUnitRepository;

    @Transactional
    public void checkAndUpdateAlerts(Product product, UUID localId, BigDecimal newQuantity) {
        String alertType = newQuantity.compareTo(BigDecimal.ZERO) <= 0
                ? "OUT_OF_STOCK"
                : newQuantity.compareTo(BigDecimal.valueOf(product.getMinStockLevel())) <= 0
                ? "LOW_STOCK" : null;

        Optional<StockAlert> existingLow = alertRepository
                .findByProductIdAndLocalIdAndTypeAndIsResolvedFalseAndIsDeletedFalse(product.getId(), localId, "LOW_STOCK");
        Optional<StockAlert> existingOut = alertRepository
                .findByProductIdAndLocalIdAndTypeAndIsResolvedFalseAndIsDeletedFalse(product.getId(), localId, "OUT_OF_STOCK");

        if (alertType == null) {
            existingLow.ifPresent(this::resolveAlert);
            existingOut.ifPresent(this::resolveAlert);
            return;
        }

        if ("OUT_OF_STOCK".equals(alertType) && existingOut.isEmpty()) {
            existingLow.ifPresent(this::resolveAlert);
            createAlert(product, localId, "OUT_OF_STOCK", AlertSeverity.CRITICAL, newQuantity, BigDecimal.ZERO);
        } else if ("LOW_STOCK".equals(alertType) && existingLow.isEmpty() && existingOut.isEmpty()) {
            createAlert(product, localId, "LOW_STOCK", AlertSeverity.WARNING, newQuantity, BigDecimal.valueOf(product.getMinStockLevel()));
        }
    }

    @Transactional
    public int checkExpiryAlerts(int daysBeforeExpiry) {
        LocalDate threshold = LocalDate.now().plusDays(daysBeforeExpiry);
        List<StockLot> expiringSoon = lotRepository.findExpiringSoon(threshold);
        int createdCount = 0;

        for (StockLot lot : expiringSoon) {
            boolean isExpired = lot.getExpiryDate().isBefore(LocalDate.now());
            String type = isExpired ? "EXPIRED" : "EXPIRY_SOON";
            AlertSeverity severity = isExpired ? AlertSeverity.CRITICAL : AlertSeverity.WARNING;

            boolean alreadyExists = alertRepository
                    .findByProductIdAndLocalIdAndTypeAndIsResolvedFalseAndIsDeletedFalse(
                            lot.getProductId(), lot.getLocalId(), type).isPresent();

            if (!alreadyExists) {
                StockAlert alert = StockAlert.builder()
                        .productId(lot.getProductId())
                        .localId(lot.getLocalId())
                        .type(type)
                        .severity(severity)
                        .currentStock(lot.getQuantity())
                        .expiryDate(lot.getExpiryDate())
                        .lotId(lot.getId())
                        .isResolved(false)
                        .build();
                alert.setCreatedAt(LocalDateTime.now());
                alertRepository.save(alert);
                createdCount++;
            }
        }
        return createdCount;
    }

    @Transactional
    public StockAlertResponse resolveAlertById(UUID alertId) {
        StockAlert alert = alertRepository.findById(alertId)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("StockAlert", alertId.toString()));
        resolveAlert(alert);
        return toResponseWithMaps(alert, getProductMap(List.of(alert)), getLocalMap(List.of(alert)), getLotMap(List.of(alert)));
    }

    private void resolveAlert(StockAlert alert) {
        alert.setIsResolved(true);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());
        alertRepository.save(alert);
    }

    private void createAlert(Product product, UUID localId, String type,
                             AlertSeverity severity, BigDecimal current, BigDecimal threshold) {
        StockAlert alert = StockAlert.builder()
                .productId(product.getId())
                .localId(localId)
                .type(type)
                .severity(severity)
                .currentStock(current)
                .threshold(threshold)
                .isResolved(false)
                .build();
        alert.setCreatedAt(LocalDateTime.now());
        alertRepository.save(alert);
    }

    @Transactional(readOnly = true)
    public List<StockAlertResponse> getActiveAlerts(UUID localId) {
        List<StockAlert> alerts = localId != null
                ? alertRepository.findByLocalIdAndIsResolvedFalseAndIsDeletedFalse(localId)
                : alertRepository.findByIsResolvedFalseAndIsDeletedFalse();

        Map<UUID, Product> productMap = getProductMap(alerts);
        Map<UUID, LocalUnit> localMap = getLocalMap(alerts);
        Map<UUID, String> lotMap = getLotMap(alerts);

        return alerts.stream()
                .map(a -> toResponseWithMaps(a, productMap, localMap, lotMap))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countActiveAlerts(UUID localId) {
        return localId != null
                ? alertRepository.countByLocalIdAndIsResolvedFalseAndIsDeletedFalse(localId)
                : alertRepository.countByIsResolvedFalseAndIsDeletedFalse();
    }

    private Map<UUID, Product> getProductMap(List<StockAlert> alerts) {
        Set<UUID> ids = alerts.stream().map(StockAlert::getProductId).collect(Collectors.toSet());
        return ids.isEmpty() ? Collections.emptyMap() :
                productRepository.findAllByIdInAndIsDeletedFalse(ids).stream()
                        .collect(Collectors.toMap(Product::getId, p -> p));
    }

    private Map<UUID, LocalUnit> getLocalMap(List<StockAlert> alerts) {
        Set<UUID> ids = alerts.stream().map(StockAlert::getLocalId).collect(Collectors.toSet());
        return ids.isEmpty() ? Collections.emptyMap() :
                localUnitRepository.findAllByIdInAndIsDeletedFalse(ids).stream()
                        .collect(Collectors.toMap(LocalUnit::getId, l -> l));
    }

    private Map<UUID, String> getLotMap(List<StockAlert> alerts) {
        Set<UUID> ids = alerts.stream().map(StockAlert::getLotId).filter(Objects::nonNull).collect(Collectors.toSet());
        return ids.isEmpty() ? Collections.emptyMap() :
                lotRepository.findAllById(ids).stream()
                        .collect(Collectors.toMap(StockLot::getId, StockLot::getLotNumber));
    }

    private StockAlertResponse toResponseWithMaps(StockAlert a, Map<UUID, Product> productMap,
                                                  Map<UUID, LocalUnit> localMap, Map<UUID, String> lotMap) {
        Product p = productMap.get(a.getProductId());
        LocalUnit l = localMap.get(a.getLocalId());
        String lotNum = a.getLotId() != null ? lotMap.get(a.getLotId()) : null;

        return StockAlertResponse.builder()
                .id(a.getId())
                .productId(a.getProductId())
                .productRef(p != null ? p.getRef() : null)
                .productCode(p != null ? p.getRef() : null)
                .productName(p != null ? p.getName() : null)
                .localId(a.getLocalId())
                .localName(l != null ? l.getName() : null)
                .type(a.getType())
                .typeLabel(alertTypeLabel(a.getType()))
                .severity(a.getSeverity())
                .currentStock(a.getCurrentStock())
                .threshold(a.getThreshold())
                .expiryDate(a.getExpiryDate())
                .lotId(a.getLotId())
                .lotNumber(lotNum)
                .isResolved(a.getIsResolved())
                .resolvedAt(a.getResolvedAt())
                .createdAt(a.getCreatedAt())
                .build();
    }

    private String alertTypeLabel(String type) {
        if (type == null) return "";
        return switch (type) {
            case "LOW_STOCK" -> "Stock faible";
            case "OUT_OF_STOCK" -> "Rupture de stock";
            case "EXPIRY_SOON" -> "Péremption proche";
            case "EXPIRED" -> "Expiré";
            default -> type;
        };
    }
}
