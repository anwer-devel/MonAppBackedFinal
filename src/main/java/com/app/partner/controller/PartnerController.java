package com.app.partner.controller;

import com.app.common.response.ApiResponse;
import com.app.common.security.SecurityUtil;
import com.app.partner.dto.*;
import com.app.partner.service.PartnerService;
import com.app.auth.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/partners")
@RequiredArgsConstructor
@Tag(name = "Partners", description = "Partner management endpoints (CRUD, location, assets)")
public class PartnerController {

    private final PartnerService partnerService;

    @GetMapping
    @Operation(
            summary = "Get Partners by Zone",
            description = "Get paginated list of partners in a specific zone with sorting",
            tags = {"Partners"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Partners retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Zone not found"
            )
    })
    public ResponseEntity<ApiResponse<Page<PartnerDto>>> getPartnersByZone(
            @RequestParam
            @Parameter(description = "Zone UUID", required = true)
            UUID zoneId,
            @RequestParam(defaultValue = "0")
            @Parameter(description = "Page number (0-based)")
            int page,
            @RequestParam(defaultValue = "20")
            @Parameter(description = "Page size")
            int size,
            @RequestParam(defaultValue = "createdAt")
            @Parameter(description = "Sort field")
            String sortBy,
            @RequestParam(defaultValue = "DESC")
            @Parameter(description = "Sort direction")
            Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<PartnerDto> partners = partnerService.getPartnersByZone(zoneId, pageable);
        return ResponseEntity.ok(ApiResponse.success(partners, "Partners retrieved successfully"));
    }

    @GetMapping("/top")
    @Operation(
            summary = "Get Top Partners by Zone",
            description = "Get top-rated partners in a specific zone (sorted by rating DESC)",
            tags = {"Partners"}
    )
    public ResponseEntity<ApiResponse<Page<PartnerDto>>> getTopPartnersByZone(
            @RequestParam
            @Parameter(description = "Zone UUID", required = true)
            UUID zoneId,
            @RequestParam(defaultValue = "0")
            @Parameter(description = "Page number")
            int page,
            @RequestParam(defaultValue = "10")
            @Parameter(description = "Page size")
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PartnerDto> partners = partnerService.getTopPartnersByZone(zoneId, pageable);
        return ResponseEntity.ok(ApiResponse.success(partners, "Top partners retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get Partner by ID",
            description = "Retrieve a specific partner by its UUID",
            tags = {"Partners"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Partner retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Partner not found"
            )
    })
    public ResponseEntity<ApiResponse<PartnerDto>> getPartner(@PathVariable UUID id) {
        PartnerDto partner = partnerService.getPartnerById(id);
        return ResponseEntity.ok(ApiResponse.success(partner, "Partner retrieved successfully"));
    }


    @GetMapping("/me/list")
    @PreAuthorize("hasRole('PARTNER_OWNER')")
    @Operation(
            summary = "Get My Partners",
            description = "Get all partners owned by the current user (Partner Owner Only)",
            tags = {"Partners"}
    )
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<ApiResponse<List<PartnerDto>>> getMyPartners() {
        UUID userId = SecurityUtil.getCurrentUserId();
        List<PartnerDto> partners = partnerService.getMyPartners(userId);
        return ResponseEntity.ok(ApiResponse.success(partners, "Your partners retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasRole('PARTNER_OWNER')")
    @Operation(
            summary = "Create Partner",
            description = "Create a new partner (Partner Owner Only)",
            tags = {"Partners"}
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Partner created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Only partner owners can create partners"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Zone not found"
            )
    })
    public ResponseEntity<ApiResponse<PartnerDto>> createPartner(
            @Valid @RequestBody CreatePartnerRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        PartnerDto partner = partnerService.createPartner(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(partner, "Partner created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PARTNER_OWNER')")
    @Operation(
            summary = "Update Partner",
            description = "Update an existing partner (Partner Owner Only)",
            tags = {"Partners"}
    )
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<ApiResponse<PartnerDto>> updatePartner(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePartnerRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        PartnerDto partner = partnerService.updatePartner(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success(partner, "Partner updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PARTNER_OWNER')")
    @Operation(
            summary = "Delete Partner",
            description = "Delete (soft delete) a partner (Partner Owner Only)",
            tags = {"Partners"}
    )
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<ApiResponse<Void>> deletePartner(@PathVariable UUID id) {
        UUID userId = SecurityUtil.getCurrentUserId();
        partnerService.deletePartner(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Partner deleted successfully"));
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Verify Partner",
            description = "Verify/approve a partner (Admin Only)",
            tags = {"Partners"}
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Partner verified successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Only admins can verify partners"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Partner not found"
            )
    })
    public ResponseEntity<ApiResponse<Void>> verifyPartner(@PathVariable UUID id) {
        partnerService.verifyPartner(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Partner verified successfully"));
    }

    // Location Endpoints
    @PostMapping("/{partnerId}/location")
    @PreAuthorize("hasRole('PARTNER_OWNER')")
    @Operation(
            summary = "Set Location",
            description = "Set or update location for a partner (Partner Owner Only)",
            tags = {"Locations"}
    )
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<ApiResponse<LocationDto>> setLocation(
            @PathVariable UUID partnerId,
            @Valid @RequestBody LocationDto locationDto) {
        UUID userId = SecurityUtil.getCurrentUserId();
        LocationDto location = partnerService.setLocation(partnerId, locationDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(location, "Location set successfully"));
    }

    @GetMapping("/{partnerId}/location")
    @Operation(
            summary = "Get Location",
            description = "Get location information for a partner",
            tags = {"Locations"}
    )
    public ResponseEntity<ApiResponse<LocationDto>> getLocation(@PathVariable UUID partnerId) {
        LocationDto location = partnerService.getLocation(partnerId);
        return ResponseEntity.ok(ApiResponse.success(location, "Location retrieved successfully"));
    }

    // Asset Endpoints
    @PostMapping("/{partnerId}/assets/upload")
    @PreAuthorize("hasRole('PARTNER_OWNER')")
    @Operation(
            summary = "Upload Asset",
            description = "Upload an image asset for a partner (JPEG, PNG) - Partner Owner Only",
            tags = {"Assets"}
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Asset uploaded successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid file or type"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "413",
                    description = "File too large (max 50MB)"
            )
    })
    public ResponseEntity<ApiResponse<PartnerAssetDto>> uploadAsset(
            @PathVariable UUID partnerId,
            @RequestParam("file")
            @Parameter(description = "Image file (JPEG, PNG, max 50MB)")
            MultipartFile file,
            @RequestParam("type")
            @Parameter(description = "Asset type: IMAGE, COVER, LOGO")
            String type) {
        UUID userId = SecurityUtil.getCurrentUserId();
        PartnerAssetDto asset = partnerService.uploadAsset(partnerId, file, type, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(asset, "Asset uploaded successfully"));
    }

    @GetMapping("/{partnerId}/assets")
    @Operation(
            summary = "Get Assets",
            description = "Get all assets (images) for a partner",
            tags = {"Assets"}
    )
    public ResponseEntity<ApiResponse<List<PartnerAssetDto>>> getAssets(@PathVariable UUID partnerId) {
        List<PartnerAssetDto> assets = partnerService.getAssets(partnerId);
        return ResponseEntity.ok(ApiResponse.success(assets, "Assets retrieved successfully"));
    }

    @DeleteMapping("/assets/{assetId}")
    @PreAuthorize("hasRole('PARTNER_OWNER')")
    @Operation(
            summary = "Delete Asset",
            description = "Delete an asset (Partner Owner Only)",
            tags = {"Assets"}
    )
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<ApiResponse<Void>> deleteAsset(@PathVariable UUID assetId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        partnerService.deleteAsset(assetId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Asset deleted successfully"));
    }
}

