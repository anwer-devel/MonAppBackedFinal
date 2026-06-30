package com.erp.platform.inventory.controller;

import com.erp.platform.core.common.PageResponse;
import com.erp.platform.inventory.dto.response.MultiLocationStockResponse;
import com.erp.platform.inventory.dto.response.StockAlertResponse;
import com.erp.platform.inventory.dto.response.StockEntryResponse;
import com.erp.platform.inventory.service.StockAlertService;
import com.erp.platform.inventory.service.StockEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockEntryService entryService;
    private final StockAlertService alertService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<MultiLocationStockResponse>> getMultiLocationView(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(entryService.getMultiLocationView(q, categoryId, status, page, size));
    }

    @GetMapping("/alerts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<StockAlertResponse>> getAlerts(@RequestParam(required = false) UUID localId) {
        return ResponseEntity.ok(alertService.getActiveAlerts(localId));
    }

    @GetMapping("/alerts/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getAlertsCount(@RequestParam(required = false) UUID localId) {
        return ResponseEntity.ok(Map.of("count", alertService.countActiveAlerts(localId)));
    }

    @GetMapping("/products/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<StockEntryResponse>> getByProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(entryService.getByProduct(productId));
    }

    @GetMapping("/products/{productId}/locals/{localId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StockEntryResponse> getByProductAndLocal(@PathVariable UUID productId, @PathVariable UUID localId) {
        return ResponseEntity.ok(entryService.getByProductAndLocal(productId, localId));
    }
}
