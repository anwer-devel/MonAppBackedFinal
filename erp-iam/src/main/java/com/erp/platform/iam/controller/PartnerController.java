package com.erp.platform.iam.controller;

import com.erp.platform.core.common.PageResponse;
import com.erp.platform.core.security.UserPrincipal;
import com.erp.platform.iam.dto.collaborator.CollaboratorResponse;
import com.erp.platform.iam.dto.partner.*;
import com.erp.platform.iam.entity.PartnerConfig;
import com.erp.platform.iam.enums.PartnerStatus;
import com.erp.platform.iam.enums.PlanType;
import com.erp.platform.iam.enums.SectorType;
import com.erp.platform.iam.service.PartnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/partners")
@RequiredArgsConstructor
@Tag(name = "Partners", description = "Partner management (Platform Admin)")
public class PartnerController {

    private final PartnerService partnerService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Get partner statistics")
    public ResponseEntity<PartnerStatsResponse> getStats() {
        return ResponseEntity.ok(partnerService.getStats());
    }

    @GetMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "List partners with filters")
    public ResponseEntity<PageResponse<PartnerResponse>> getAll(
            @RequestParam(required = false) SectorType sector,
            @RequestParam(required = false) PlanType plan,
            @RequestParam(required = false) PartnerStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sortObj = direction.equalsIgnoreCase("desc")
                ? Sort.by(sort).descending()
                : Sort.by(sort).ascending();
        Pageable pageable = PageRequest.of(page, size, sortObj);

        return ResponseEntity.ok(partnerService.getAll(sector, plan, status, q, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Get partner by ID")
    public ResponseEntity<PartnerResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(partnerService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Create a new partner")
    public ResponseEntity<PartnerResponse> create(
            @Valid @RequestBody CreatePartnerRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        PartnerResponse response = partnerService.create(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Update partner details")
    public ResponseEntity<PartnerResponse> update(@PathVariable UUID id,
                                                    @Valid @RequestBody UpdatePartnerRequest request) {
        return ResponseEntity.ok(partnerService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Update partner status")
    public ResponseEntity<PartnerResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePartnerStatusRequest request) {
        return ResponseEntity.ok(partnerService.updateStatus(id, request.getStatus()));
    }

    @GetMapping("/{id}/config")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Get partner configuration")
    public ResponseEntity<PartnerConfig> getConfig(@PathVariable UUID id) {
        return ResponseEntity.ok(partnerService.getConfig(id));
    }

    @PutMapping("/{id}/config")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Update partner configuration")
    public ResponseEntity<PartnerResponse> updateConfig(
            @PathVariable UUID id,
            @Valid @RequestBody PartnerConfigRequest request) {
        return ResponseEntity.ok(partnerService.updateConfig(id, request));
    }

    @PostMapping("/{id}/admin-user")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Create partner admin user")
    public ResponseEntity<CollaboratorResponse> createAdmin(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePartnerAdminRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        CollaboratorResponse response = partnerService.createAdmin(id, request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/setup-status")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','PARTNER_ADMIN')")
    @Operation(summary = "Get partner setup status")
    public ResponseEntity<SetupStatusResponse> getSetupStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(partnerService.getSetupStatus(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Soft delete a partner")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        partnerService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
