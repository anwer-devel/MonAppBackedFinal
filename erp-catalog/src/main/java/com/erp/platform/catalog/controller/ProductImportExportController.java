package com.erp.platform.catalog.controller;

import com.erp.platform.catalog.dto.response.ImportResultResponse;
import com.erp.platform.catalog.service.ProductImportExportService;
import com.erp.platform.core.security.JwtUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Import/Export", description = "Import et export CSV du catalogue")
public class ProductImportExportController {

    private final ProductImportExportService importExportService;

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'COLLAB_MANAGING_DIRECTOR', 'COLLAB_OPERATIONS_MANAGER')")
    @Operation(summary = "Importer des produits depuis un fichier CSV")
    public ResponseEntity<ImportResultResponse> importCsv(@RequestParam("file") MultipartFile file, Authentication auth) {
        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        UUID partnerId = UUID.fromString(principal.getPartnerId());
        return ResponseEntity.ok(importExportService.importCsv(file));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('PARTNER_ADMIN', 'COLLAB_MANAGING_DIRECTOR', 'COLLAB_OPERATIONS_MANAGER', 'COLLAB_INVENTORY_MANAGER')")
    @Operation(summary = "Exporter l'ensemble du catalogue en CSV")
    public void exportCsv(HttpServletResponse response, Authentication auth) {
        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        UUID partnerId = UUID.fromString(principal.getPartnerId());
        importExportService.exportCsv(response);
    }
}
