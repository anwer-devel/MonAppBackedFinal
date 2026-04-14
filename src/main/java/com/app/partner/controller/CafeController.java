package com.app.partner.controller;

import com.app.common.response.ApiResponse;
import com.app.common.security.SecurityUtil;
import com.app.partner.dto.CafeDTO;
import com.app.partner.service.CafeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cafes")
@RequiredArgsConstructor
@Tag(name = "Cafes", description = "Cafe discovery and details")
public class CafeController {

    private final CafeService cafeService;

    @GetMapping
    @Operation(summary = "List all cafes")
    public ResponseEntity<ApiResponse<List<CafeDTO>>> getAllCafes(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false, defaultValue = "5000") Double radius) {
        List<CafeDTO> cafes = cafeService.getAllCafes(lat, lng, radius);
        return ResponseEntity.ok(ApiResponse.success(cafes, "Cafes retrieved"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get cafe details")
    public ResponseEntity<ApiResponse<CafeDTO>> getCafe(@PathVariable UUID id) {
        CafeDTO cafe = cafeService.getCafe(id);
        return ResponseEntity.ok(ApiResponse.success(cafe, "Cafe retrieved"));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Get nearby cafes")
    public ResponseEntity<ApiResponse<List<CafeDTO>>> getNearbyCafes(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "2000") Double radius) {
        List<CafeDTO> cafes = cafeService.getNearbyCafes(lat, lng, radius);
        return ResponseEntity.ok(ApiResponse.success(cafes, "Nearby cafes retrieved"));
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending cafes")
    public ResponseEntity<ApiResponse<List<CafeDTO>>> getTrendingCafes() {
        List<CafeDTO> cafes = cafeService.getTrendingCafes();
        return ResponseEntity.ok(ApiResponse.success(cafes, "Trending cafes retrieved"));
    }

    @PostMapping("/{id}/like")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Toggle like on cafe")
    public ResponseEntity<ApiResponse<Void>> toggleLike(@PathVariable UUID id) {
        UUID userId = SecurityUtil.getCurrentUserId();
        cafeService.toggleLike(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Like toggled"));
    }
}
