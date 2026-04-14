package com.app.partner.service;

import com.app.partner.dto.CafeDTO;
import com.app.partner.dto.VenueDTO;

import java.util.List;
import java.util.UUID;

public interface CafeService {
    List<CafeDTO> getAllCafes(Double lat, Double lng, Double radius);
    CafeDTO getCafe(UUID id);
    List<CafeDTO> getNearbyCafes(Double lat, Double lng, Double radius);
    List<CafeDTO> getTrendingCafes();
    void toggleLike(UUID cafeId, UUID userId);
    List<VenueDTO> getVenueMarkers(Double lat, Double lng, UUID userId);
    VenueDTO getVenueDetail(UUID id, UUID userId);
    void toggleFavorite(UUID venueId, UUID userId);
}
