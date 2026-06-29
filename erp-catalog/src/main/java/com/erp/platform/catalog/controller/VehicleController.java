package com.erp.platform.catalog.controller;

import com.erp.platform.catalog.dto.request.VehicleSearchRequest;
import com.erp.platform.catalog.dto.response.ProductSummaryResponse;
import com.erp.platform.catalog.service.VehicleCompatibilityService;
import com.erp.platform.core.security.JwtUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Recherche par compatibilité véhicule (Secteur AUTO)")
public class VehicleController {

    private final VehicleCompatibilityService compatibilityService;

    @GetMapping("/makes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les marques de véhicules distinctes")
    public ResponseEntity<List<String>> getMakes(Authentication auth) {
        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        UUID partnerId = UUID.fromString(principal.getPartnerId());
        return ResponseEntity.ok(compatibilityService.getDistinctMakes());
    }

    @GetMapping("/models")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les modèles pour une marque donnée")
    public ResponseEntity<List<String>> getModels(@RequestParam String make, Authentication auth) {
        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        UUID partnerId = UUID.fromString(principal.getPartnerId());
        return ResponseEntity.ok(compatibilityService.getModelsByMake(make));
    }

    @PostMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Rechercher des produits compatibles avec un véhicule")
    public ResponseEntity<List<ProductSummaryResponse>> searchCompatible(@Valid @RequestBody VehicleSearchRequest request, Authentication auth) {
        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        UUID partnerId = UUID.fromString(principal.getPartnerId());
        return ResponseEntity.ok(compatibilityService.findCompatibleProducts(request));
    }
}
