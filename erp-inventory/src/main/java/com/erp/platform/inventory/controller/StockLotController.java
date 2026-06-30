package com.erp.platform.inventory.controller;

import com.erp.platform.inventory.dto.request.CreateLotRequest;
import com.erp.platform.inventory.dto.response.StockLotResponse;
import com.erp.platform.inventory.service.StockLotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stock/lots")
@RequiredArgsConstructor
public class StockLotController {

    private final StockLotService lotService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'STOCK_MANAGER')")
    public ResponseEntity<StockLotResponse> createLot(@Valid @RequestBody CreateLotRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lotService.createLot(req));
    }

    @GetMapping("/products/{productId}/locals/{localId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<StockLotResponse>> getLotsByProductAndLocal(@PathVariable UUID productId, @PathVariable UUID localId) {
        return ResponseEntity.ok(lotService.getByProductAndLocal(productId, localId));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<StockLotResponse>> getLots(@RequestParam UUID productId, @RequestParam UUID localId) {
        return ResponseEntity.ok(lotService.getByProductAndLocal(productId, localId));
    }

    @GetMapping("/expiring-soon")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<StockLotResponse>> getExpiringSoon(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(lotService.getExpiringSoon(days));
    }
}
