package com.erp.platform.catalog.controller;

import com.erp.platform.catalog.dto.request.CreateUnitRequest;
import com.erp.platform.catalog.dto.response.UnitResponse;
import com.erp.platform.catalog.service.UnitService;
import com.erp.platform.core.security.JwtUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/units")
@RequiredArgsConstructor
@Tag(name = "Units", description = "Gestion des unités de mesure")
public class UnitController {

    private final UnitService unitService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les unités de mesure")
    public ResponseEntity<List<UnitResponse>> getAll(Authentication auth) {
        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        UUID partnerId = UUID.fromString(principal.getPartnerId());
        return ResponseEntity.ok(unitService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'COLLAB_MANAGING_DIRECTOR', 'COLLAB_OPERATIONS_MANAGER')")
    @Operation(summary = "Créer une unité de mesure")
    public ResponseEntity<UnitResponse> create(@Valid @RequestBody CreateUnitRequest request, Authentication auth) {
        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        UUID partnerId = UUID.fromString(principal.getPartnerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(unitService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'COLLAB_MANAGING_DIRECTOR', 'COLLAB_OPERATIONS_MANAGER')")
    @Operation(summary = "Modifier une unité de mesure")
    public ResponseEntity<UnitResponse> update(@PathVariable UUID id, @Valid @RequestBody CreateUnitRequest request, Authentication auth) {
        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        UUID partnerId = UUID.fromString(principal.getPartnerId());
        return ResponseEntity.ok(unitService.update(id, request));
    }
}
