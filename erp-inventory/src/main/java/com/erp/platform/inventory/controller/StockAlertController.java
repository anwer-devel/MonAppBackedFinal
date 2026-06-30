package com.erp.platform.inventory.controller;

import com.erp.platform.inventory.dto.response.StockAlertResponse;
import com.erp.platform.inventory.service.StockAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stock/alerts")
@RequiredArgsConstructor
public class StockAlertController {

    private final StockAlertService alertService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<StockAlertResponse>> getActiveAlerts(@RequestParam(required = false) UUID localId) {
        return ResponseEntity.ok(alertService.getActiveAlerts(localId));
    }

    @PostMapping("/check-expiry")
    @PreAuthorize("hasRole('PARTNER_ADMIN')")
    public ResponseEntity<Map<String, Integer>> checkExpiry(@RequestParam(defaultValue = "30") int daysBeforeExpiry) {
        return ResponseEntity.ok(Map.of("alertsCreated", alertService.checkExpiryAlerts(daysBeforeExpiry)));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'STOCK_MANAGER')")
    public ResponseEntity<StockAlertResponse> resolveAlert(@PathVariable UUID id) {
        return ResponseEntity.ok(alertService.resolveAlertById(id));
    }

    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> countActiveAlerts(@RequestParam(required = false) UUID localId) {
        return ResponseEntity.ok(Map.of("count", alertService.countActiveAlerts(localId)));
    }
}
