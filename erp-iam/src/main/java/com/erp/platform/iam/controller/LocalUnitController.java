package com.erp.platform.iam.controller;

import com.erp.platform.core.security.JwtUserPrincipal;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/locals")
@RequiredArgsConstructor
@Tag(name = "Local Units", description = "Local unit management")
public class LocalUnitController {

    private final LocalUnitService localUnitService;

    private JwtUserPrincipal getPrincipal(Authentication auth) {
        return (JwtUserPrincipal) auth.getPrincipal();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List local units by partner")
    public ResponseEntity<List<LocalResponse>> getByPartner(
            @RequestParam(required = false) UUID partnerId,
            @RequestParam(required = false) LocalType type,
            Authentication auth) {

        JwtUserPrincipal principal = getPrincipal(auth);
        UUID resolvedPartnerId = partnerId != null ? partnerId :
                (principal.getPartnerId() != null ? UUID.fromString(principal.getPartnerId()) : null);

        if (resolvedPartnerId == null) {
            return ResponseEntity.ok(List.of());
        }

        UUID userId = UUID.fromString(principal.getUserId());
        List<LocalResponse> locals = localUnitService.getByPartner(resolvedPartnerId, type, userId);
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
            Authentication auth) {

        JwtUserPrincipal principal = getPrincipal(auth);
        UUID resolvedPartnerId = partnerId != null ? partnerId :
                (request.getPartnerId() != null ? request.getPartnerId() :
                        (principal.getPartnerId() != null ? UUID.fromString(principal.getPartnerId()) : null));

        if (resolvedPartnerId == null) {
            throw new IllegalArgumentException("partnerId est requis pour créer un local");
        }

        UUID userId = UUID.fromString(principal.getUserId());
        LocalResponse response = localUnitService.create(resolvedPartnerId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','PARTNER_ADMIN')")
    @Operation(summary = "Update a local unit")
    public ResponseEntity<LocalResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody UpdateLocalRequest request,
                                                  Authentication auth) {
        UUID userId = UUID.fromString(getPrincipal(auth).getUserId());
        return ResponseEntity.ok(localUnitService.update(id, request, userId));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','PARTNER_ADMIN')")
    @Operation(summary = "Update local unit status")
    public ResponseEntity<LocalResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLocalStatusRequest request,
            Authentication auth) {
        UUID userId = UUID.fromString(getPrincipal(auth).getUserId());
        return ResponseEntity.ok(localUnitService.updateStatus(id, request.getStatus(), userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','PARTNER_ADMIN')")
    @Operation(summary = "Soft delete a local unit")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       Authentication auth) {
        UUID userId = UUID.fromString(getPrincipal(auth).getUserId());
        localUnitService.softDelete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
