package com.erp.platform.catalog.controller;

import com.erp.platform.catalog.dto.request.CreateProductRequest;
import com.erp.platform.catalog.dto.request.UpdateProductRequest;
import com.erp.platform.catalog.dto.response.ProductResponse;
import com.erp.platform.catalog.dto.response.ProductSummaryResponse;
import com.erp.platform.catalog.service.ProductService;
import com.erp.platform.core.common.PageResponse;
import com.erp.platform.core.exception.ResourceNotFoundException;
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
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Gestion du catalogue produits")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Rechercher et filtrer les produits avec pagination")
    public ResponseEntity<PageResponse<ProductSummaryResponse>> search(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String sectorType,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isFavorite,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort,
            Authentication auth) {

        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        UUID partnerId = UUID.fromString(principal.getPartnerId());
        return ResponseEntity.ok(productService.search(categoryId, sectorType, isActive, isFavorite, q, page, size, sort));
    }

    @GetMapping("/scan")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Recherche rapide par code-barres ou référence (Scan POS)")
    public ResponseEntity<ProductSummaryResponse> scanLookup(@RequestParam String code, Authentication auth) {
        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        UUID partnerId = UUID.fromString(principal.getPartnerId());
        return productService.scanLookup(code)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "code", code));
    }

    @GetMapping("/favorites")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Récupérer la liste des produits favoris (Caisse POS)")
    public ResponseEntity<List<ProductSummaryResponse>> getFavorites(Authentication auth) {
        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        UUID partnerId = UUID.fromString(principal.getPartnerId());
        return ResponseEntity.ok(productService.getFavorites());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Détail complet d'un produit avec ses extensions sectorielles")
    public ResponseEntity<ProductResponse> getById(@PathVariable UUID id, Authentication auth) {
        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        UUID partnerId = UUID.fromString(principal.getPartnerId());
        return ResponseEntity.ok(productService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'COLLAB_MANAGING_DIRECTOR', 'COLLAB_OPERATIONS_MANAGER', 'COLLAB_INVENTORY_MANAGER')")
    @Operation(summary = "Créer un nouveau produit")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request, Authentication auth) {
        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        UUID partnerId = UUID.fromString(principal.getPartnerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'COLLAB_MANAGING_DIRECTOR', 'COLLAB_OPERATIONS_MANAGER', 'COLLAB_INVENTORY_MANAGER')")
    @Operation(summary = "Mettre à jour un produit")
    public ResponseEntity<ProductResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateProductRequest request, Authentication auth) {
        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        UUID partnerId = UUID.fromString(principal.getPartnerId());
        return ResponseEntity.ok(productService.update(id, request));
    }

    @PatchMapping("/{id}/toggle-favorite")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Basculer l'état favori (POS)")
    public ResponseEntity<Void> toggleFavorite(@PathVariable UUID id, Authentication auth) {
        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        UUID partnerId = UUID.fromString(principal.getPartnerId());
        productService.toggleFavorite(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'COLLAB_MANAGING_DIRECTOR', 'COLLAB_OPERATIONS_MANAGER', 'COLLAB_INVENTORY_MANAGER')")
    @Operation(summary = "Activer ou désactiver un produit")
    public ResponseEntity<Void> toggleActive(@PathVariable UUID id, Authentication auth) {
        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        UUID partnerId = UUID.fromString(principal.getPartnerId());
        productService.toggleActive(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'COLLAB_MANAGING_DIRECTOR')")
    @Operation(summary = "Suppression logique d'un produit")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication auth) {
        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        UUID partnerId = UUID.fromString(principal.getPartnerId());
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
