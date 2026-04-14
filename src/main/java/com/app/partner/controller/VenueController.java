package com.app.partner.controller;

import com.app.common.response.ApiResponse;
import com.app.common.security.SecurityUtil;
import com.app.partner.dto.VenueDTO;
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
@RequestMapping("/api/venues")
@RequiredArgsConstructor
@Tag(name = "Venues", description = "Map venue markers and details")
public class VenueController {

    private final CafeService cafeService;

    @GetMapping
    @Operation(summary = "Get all venue markers for map")
    public ResponseEntity<ApiResponse<List<VenueDTO>>> getVenues(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng) {
        UUID userId = SecurityUtil.getCurrentUserId();
        List<VenueDTO> venues = cafeService.getVenueMarkers(lat, lng, userId);
        return ResponseEntity.ok(ApiResponse.success(venues, "Venues retrieved"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get venue detail")
    public ResponseEntity<ApiResponse<VenueDTO>> getVenue(@PathVariable UUID id) {
        UUID userId = SecurityUtil.getCurrentUserId();
        VenueDTO venue = cafeService.getVenueDetail(id, userId);
        return ResponseEntity.ok(ApiResponse.success(venue, "Venue retrieved"));
    }


    @PostMapping("/{id}/favorite")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Toggle favorite venue")
    public ResponseEntity<ApiResponse<Void>> toggleFavorite(@PathVariable UUID id) {
        UUID userId = SecurityUtil.getCurrentUserId();
        cafeService.toggleFavorite(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Favorite toggled"));
    }
}
