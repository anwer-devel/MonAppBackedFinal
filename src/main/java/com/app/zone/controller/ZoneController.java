package com.app.zone.controller;

import com.app.common.response.ApiResponse;
import com.app.zone.dto.CreateZoneRequest;
import com.app.zone.dto.ZoneDto;
import com.app.zone.service.ZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
@Tag(name = "Zones", description = "Zone management endpoints (Admin Only)")
public class ZoneController {

    private final ZoneService zoneService;

    @GetMapping
    @Operation(
            summary = "List All Zones",
            description = "Get a list of all active zones",
            tags = {"Zones"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Zones retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ZoneDto[].class))
            )
    })
    public ResponseEntity<ApiResponse<List<ZoneDto>>> getAllZones() {
        List<ZoneDto> zones = zoneService.getAllZones();
        return ResponseEntity.ok(ApiResponse.success(zones, "Zones retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get Zone by ID",
            description = "Retrieve a specific zone by its UUID",
            tags = {"Zones"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Zone retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ZoneDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Zone not found"
            )
    })
    public ResponseEntity<ApiResponse<ZoneDto>> getZoneById(@PathVariable UUID id) {
        ZoneDto zone = zoneService.getZoneById(id);
        return ResponseEntity.ok(ApiResponse.success(zone, "Zone retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create New Zone",
            description = "Create a new zone (Admin Only)",
            tags = {"Zones"}
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Zone created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ZoneDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Only admins can create zones"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input"
            )
    })
    public ResponseEntity<ApiResponse<ZoneDto>> createZone(@Valid @RequestBody CreateZoneRequest request) {
        ZoneDto zone = zoneService.createZone(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(zone, "Zone created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update Zone",
            description = "Update an existing zone (Admin Only)",
            tags = {"Zones"}
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Zone updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ZoneDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Only admins can update zones"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Zone not found"
            )
    })
    public ResponseEntity<ApiResponse<ZoneDto>> updateZone(
            @PathVariable UUID id,
            @Valid @RequestBody CreateZoneRequest request) {
        ZoneDto zone = zoneService.updateZone(id, request);
        return ResponseEntity.ok(ApiResponse.success(zone, "Zone updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete Zone",
            description = "Delete (soft delete) a zone (Admin Only)",
            tags = {"Zones"}
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Zone deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Only admins can delete zones"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Zone not found"
            )
    })
    public ResponseEntity<ApiResponse<Void>> deleteZone(@PathVariable UUID id) {
        zoneService.deleteZone(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Zone deleted successfully"));
    }
}

