package com.erp.platform.iam.controller;

import com.erp.platform.core.security.UserPrincipal;
import com.erp.platform.iam.dto.local.*;
import com.erp.platform.iam.enums.LocalType;
import com.erp.platform.iam.service.LocalUnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/locals")
@RequiredArgsConstructor
@Tag(name = "Local Units", description = "Local unit management")
public class LocalUnitController {

    private final LocalUnitService localUnitService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List local units by partner")
    public ResponseEntity<List<LocalResponse>> getByPartner(
            @RequestParam(required = false) UUID partnerId,
            @RequestParam(required = false) LocalType type,
            @AuthenticationPrincipal UserPrincipal principal) {

        // Auto-inject partnerId from JWT for non-platform-admin users
        UUID resolvedPartnerId = partnerId != null ? partnerId : principal.getPartnerId();
        if (resolvedPartnerId == null) {
            return ResponseEntity.ok(List.of());
        }

        List<LocalResponse> locals = localUnitService.getByPartner(
                resolvedPartnerId, type, principal.getId());
        return ResponseEntity.ok(locals);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get local unit by ID")
    public ResponseEntity<LocalResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(localUnitService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','PARTNER_ADMIN')")
    @Operation(summary = "Create a local unit")
    public ResponseEntity<LocalResponse> create(
            @RequestParam(required = false) UUID partnerId,
            @Valid @RequestBody CreateLocalRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        UUID resolvedPartnerId = partnerId != null ? partnerId : principal.getPartnerId();
        LocalResponse response = localUnitService.create(
                resolvedPartnerId, request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','PARTNER_ADMIN')")
    @Operation(summary = "Update a local unit")
    public ResponseEntity<LocalResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody UpdateLocalRequest request,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(localUnitService.update(id, request, principal.getId()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','PARTNER_ADMIN')")
    @Operation(summary = "Update local unit status")
    public ResponseEntity<LocalResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLocalStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                localUnitService.updateStatus(id, request.getStatus(), principal.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','PARTNER_ADMIN')")
    @Operation(summary = "Soft delete a local unit")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                        @AuthenticationPrincipal UserPrincipal principal) {
        localUnitService.softDelete(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
