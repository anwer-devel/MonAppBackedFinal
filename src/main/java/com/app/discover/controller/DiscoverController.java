package com.app.discover.controller;

import com.app.common.response.ApiResponse;
import com.app.event.dto.EventDto;
import com.app.event.service.EventService;
import com.app.partner.dto.CafeDTO;
import com.app.partner.service.CafeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discover")
@RequiredArgsConstructor
@Tag(name = "Discover", description = "Discovery feed endpoints")
public class DiscoverController {

    private final CafeService cafeService;
    private final EventService eventService;

    @GetMapping("/trending-cafes")
    @Operation(summary = "Get trending cafes")
    public ResponseEntity<ApiResponse<List<CafeDTO>>> getTrendingCafes() {
        return ResponseEntity.ok(ApiResponse.success(cafeService.getTrendingCafes(), "Trending cafes retrieved"));
    }

    @GetMapping("/trending-events")
    @Operation(summary = "Get trending events")
    public ResponseEntity<ApiResponse<Page<EventDto>>> getTrendingEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<EventDto> events = eventService.getTrendingEvents(pageable);
        return ResponseEntity.ok(ApiResponse.success(events, "Trending events retrieved"));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Discover nearby cafes and events")
    public ResponseEntity<ApiResponse<Map<String, Object>>> discoverNearby(
            @RequestParam @NotNull(message = "Latitude is required") 
            @Min(value = -90, message = "Latitude must be between -90 and 90")
            @Max(value = 90, message = "Latitude must be between -90 and 90")
            Double lat,
            
            @RequestParam @NotNull(message = "Longitude is required")
            @Min(value = -180, message = "Longitude must be between -180 and 180")
            @Max(value = 180, message = "Longitude must be between -180 and 180")
            Double lng,
            
            @RequestParam(defaultValue = "10") 
            @Min(value = 1, message = "Radius must be at least 1 km")
            @Max(value = 500, message = "Radius cannot exceed 500 km")
            Double radius) {
        List<CafeDTO> nearbyCafes = cafeService.getNearbyCafes(lat, lng, radius);

        Map<String, Object> result = Map.of(
                "cafes", nearbyCafes,
                "cafeCount", nearbyCafes.size()
        );

        return ResponseEntity.ok(ApiResponse.success(result, "Nearby discoveries retrieved"));
    }
}
