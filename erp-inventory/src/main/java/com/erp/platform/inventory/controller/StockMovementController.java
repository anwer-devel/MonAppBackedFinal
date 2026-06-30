package com.erp.platform.inventory.controller;

import com.erp.platform.core.common.PageResponse;
import com.erp.platform.core.security.JwtUserPrincipal;
import com.erp.platform.inventory.dto.request.CreateMovementRequest;
import com.erp.platform.inventory.dto.request.StockAdjustmentRequest;
import com.erp.platform.inventory.dto.request.StockTransferRequest;
import com.erp.platform.inventory.dto.response.StockMovementResponse;
import com.erp.platform.inventory.enums.MovementType;
import com.erp.platform.inventory.service.StockMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stock/movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService movementService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'STOCK_MANAGER')")
    public ResponseEntity<StockMovementResponse> createMovement(@Valid @RequestBody CreateMovementRequest req,
                                                                Authentication auth) {
        JwtUserPrincipal principal = extractPrincipal(auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(movementService.createMovement(req, principal));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'STOCK_MANAGER')")
    public ResponseEntity<List<StockMovementResponse>> transfer(@Valid @RequestBody StockTransferRequest req,
                                                                Authentication auth) {
        JwtUserPrincipal principal = extractPrincipal(auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(movementService.transfer(req, principal));
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'STOCK_MANAGER')")
    public ResponseEntity<StockMovementResponse> adjust(@Valid @RequestBody StockAdjustmentRequest req,
                                                        Authentication auth) {
        JwtUserPrincipal principal = extractPrincipal(auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(movementService.adjust(req, principal));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'STOCK_MANAGER', 'STOCK_VIEWER')")
    public ResponseEntity<PageResponse<StockMovementResponse>> search(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID localId,
            @RequestParam(required = false) MovementType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(movementService.search(productId, localId, type, dateFrom, dateTo, PageRequest.of(page, size)));
    }

    private JwtUserPrincipal extractPrincipal(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof JwtUserPrincipal) {
            return (JwtUserPrincipal) auth.getPrincipal();
        }
        return null;
    }
}
