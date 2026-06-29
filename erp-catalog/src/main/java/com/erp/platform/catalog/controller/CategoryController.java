package com.erp.platform.catalog.controller;

import com.erp.platform.catalog.dto.request.CreateCategoryRequest;
import com.erp.platform.catalog.dto.response.CategoryResponse;
import com.erp.platform.catalog.service.CategoryService;
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
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Gestion de la taxonomie des produits")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/tree")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Récupérer l'arbre complet des catégories")
    public ResponseEntity<List<CategoryResponse>> getTree(Authentication auth) {
        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        UUID partnerId = UUID.fromString(principal.getPartnerId());
        return ResponseEntity.ok(categoryService.getTree());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'COLLAB_MANAGING_DIRECTOR', 'COLLAB_OPERATIONS_MANAGER')")
    @Operation(summary = "Créer une nouvelle catégorie")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request, Authentication auth) {
        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        UUID partnerId = UUID.fromString(principal.getPartnerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'COLLAB_MANAGING_DIRECTOR', 'COLLAB_OPERATIONS_MANAGER')")
    @Operation(summary = "Mettre à jour une catégorie")
    public ResponseEntity<CategoryResponse> update(@PathVariable UUID id, @Valid @RequestBody CreateCategoryRequest request, Authentication auth) {
        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        UUID partnerId = UUID.fromString(principal.getPartnerId());
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'COLLAB_MANAGING_DIRECTOR', 'COLLAB_OPERATIONS_MANAGER')")
    @Operation(summary = "Activer ou désactiver une catégorie")
    public ResponseEntity<Void> toggleActive(@PathVariable UUID id, Authentication auth) {
        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        UUID partnerId = UUID.fromString(principal.getPartnerId());
        categoryService.toggleActive(id);
        return ResponseEntity.noContent().build();
    }
}
