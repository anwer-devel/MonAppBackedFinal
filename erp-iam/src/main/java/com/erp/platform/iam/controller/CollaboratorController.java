package com.erp.platform.iam.controller;

import com.erp.platform.core.common.PageResponse;
import com.erp.platform.core.security.UserPrincipal;
import com.erp.platform.iam.dto.collaborator.*;
import com.erp.platform.iam.enums.CollaboratorRole;
import com.erp.platform.iam.service.CollaboratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/collaborators")
@RequiredArgsConstructor
@Tag(name = "Collaborators", description = "Collaborator management")
public class CollaboratorController {

    private final CollaboratorService collaboratorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','PARTNER_ADMIN')")
    @Operation(summary = "List collaborators with filters")
    public ResponseEntity<PageResponse<CollaboratorResponse>> getAll(
            @RequestParam(required = false) UUID partnerId,
            @RequestParam(required = false) CollaboratorRole role,
            @RequestParam(required = false) UUID localId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal principal) {

        // Auto-inject partnerId for partner admins
        UUID resolvedPartnerId = partnerId != null ? partnerId : principal.getPartnerId();
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(collaboratorService.getAll(
                resolvedPartnerId, role, localId, q, pageable, principal.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get collaborator by ID")
    public ResponseEntity<CollaboratorResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(collaboratorService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','PARTNER_ADMIN')")
    @Operation(summary = "Create a collaborator")
    public ResponseEntity<CollaboratorResponse> create(
            @RequestParam(required = false) UUID partnerId,
            @Valid @RequestBody CreateCollaboratorRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        UUID resolvedPartnerId = partnerId != null ? partnerId : principal.getPartnerId();
        CollaboratorResponse response = collaboratorService.create(
                resolvedPartnerId, request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','PARTNER_ADMIN')")
    @Operation(summary = "Update collaborator info")
    public ResponseEntity<CollaboratorResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCollaboratorRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                collaboratorService.update(id, request, principal.getId()));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','PARTNER_ADMIN')")
    @Operation(summary = "Update collaborator role")
    public ResponseEntity<CollaboratorResponse> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCollaboratorRoleRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                collaboratorService.updateRole(id, request.getRole(), principal.getId()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','PARTNER_ADMIN')")
    @Operation(summary = "Update collaborator status")
    public ResponseEntity<CollaboratorResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCollaboratorStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                collaboratorService.updateStatus(id, request.getStatus(), principal.getId()));
    }

    @PatchMapping("/{id}/locals")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','PARTNER_ADMIN')")
    @Operation(summary = "Assign locals to collaborator")
    public ResponseEntity<CollaboratorResponse> assignLocals(
            @PathVariable UUID id,
            @Valid @RequestBody AssignLocalsRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                collaboratorService.assignLocals(id, request.getLocalAccess(), principal.getId()));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','PARTNER_ADMIN')")
    @Operation(summary = "Reset collaborator password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                collaboratorService.resetPassword(id, principal.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','PARTNER_ADMIN')")
    @Operation(summary = "Soft delete a collaborator")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                        @AuthenticationPrincipal UserPrincipal principal) {
        collaboratorService.softDelete(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
