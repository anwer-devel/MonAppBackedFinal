package com.erp.platform.inventory.controller;

import com.erp.platform.core.security.JwtUserPrincipal;
import com.erp.platform.inventory.dto.request.CreateInventoryRequest;
import com.erp.platform.inventory.dto.request.UpdateInventoryLineRequest;
import com.erp.platform.inventory.dto.response.InventoryLineResponse;
import com.erp.platform.inventory.dto.response.InventoryResponse;
import com.erp.platform.inventory.service.PhysicalInventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stock/inventories")
@RequiredArgsConstructor
public class PhysicalInventoryController {

    private final PhysicalInventoryService inventoryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'STOCK_MANAGER')")
    public ResponseEntity<InventoryResponse> createInventory(@Valid @RequestBody CreateInventoryRequest req,
                                                             Authentication auth) {
        JwtUserPrincipal principal = extractPrincipal(auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createInventory(req, principal));
    }

    @PatchMapping("/{id}/lines/{lineId}")
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'STOCK_MANAGER')")
    public ResponseEntity<InventoryLineResponse> patchLine(@PathVariable UUID id, @PathVariable UUID lineId,
                                                           @Valid @RequestBody UpdateInventoryLineRequest req) {
        return ResponseEntity.ok(inventoryService.updateLine(id, lineId, req));
    }

    @PutMapping("/{id}/lines/{lineId}")
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'STOCK_MANAGER')")
    public ResponseEntity<InventoryLineResponse> updateLine(@PathVariable UUID id, @PathVariable UUID lineId,
                                                            @Valid @RequestBody UpdateInventoryLineRequest req) {
        return ResponseEntity.ok(inventoryService.updateLine(id, lineId, req));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'STOCK_MANAGER')")
    public ResponseEntity<InventoryResponse> completeInventory(@PathVariable UUID id, Authentication auth) {
        JwtUserPrincipal principal = extractPrincipal(auth);
        return ResponseEntity.ok(inventoryService.completeInventory(id, principal));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'STOCK_MANAGER', 'STOCK_VIEWER')")
    public ResponseEntity<List<InventoryResponse>> getInventories(@RequestParam(required = false) UUID localId) {
        return ResponseEntity.ok(inventoryService.getByLocal(localId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'STOCK_MANAGER', 'STOCK_VIEWER')")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable UUID id) {
        return ResponseEntity.ok(inventoryService.getById(id));
    }

    private JwtUserPrincipal extractPrincipal(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof JwtUserPrincipal) {
            return (JwtUserPrincipal) auth.getPrincipal();
        }
        return null;
    }
}
