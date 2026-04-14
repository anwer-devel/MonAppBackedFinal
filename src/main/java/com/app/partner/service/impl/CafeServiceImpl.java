package com.app.partner.service.impl;

import com.app.auth.entity.User;
import com.app.auth.repository.UserRepository;
import com.app.common.exception.ResourceNotFoundException;
import com.app.partner.dto.CafeDTO;
import com.app.partner.dto.VenueDTO;
import com.app.partner.entity.Location;
import com.app.partner.entity.Partner;
import com.app.partner.entity.UserFavoriteVenue;
import com.app.partner.repository.LocationRepository;
import com.app.partner.repository.PartnerRepository;
import com.app.partner.repository.UserFavoriteVenueRepository;
import com.app.partner.service.CafeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CafeServiceImpl implements CafeService {

    private final PartnerRepository partnerRepository;
    private final LocationRepository locationRepository;
    private final UserFavoriteVenueRepository favoriteRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CafeDTO> getAllCafes(Double lat, Double lng, Double radius) {
        List<Partner> partners = partnerRepository.findAll().stream()
                .filter(p -> p.getIsActive() && p.getType() == Partner.PartnerType.CAFE)
                // CRITICAL: Exclude partners without location
                .filter(p -> {
                    Optional<Location> location = locationRepository.findAll().stream()
                            .filter(l -> l.getPartner().getId().equals(p.getId()))
                            .findFirst();
                    return location.isPresent() && location.get().getLatitude() != null && location.get().getLongitude() != null;
                })
                .collect(Collectors.toList());

        return partners.stream()
                .map(p -> toCafeDTO(p, lat, lng))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CafeDTO getCafe(UUID id) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cafe not found: " + id));
        return toCafeDTO(partner, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CafeDTO> getNearbyCafes(Double lat, Double lng, Double radius) {
        return getAllCafes(lat, lng, radius).stream()
                .filter(c -> c.getDistance() != null && c.getDistance() <= radius)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CafeDTO> getTrendingCafes() {
        return partnerRepository.findAll().stream()
                .filter(p -> p.getIsActive() && p.getType() == Partner.PartnerType.CAFE)
                .sorted((a, b) -> Double.compare(b.getRating(), a.getRating()))
                .limit(10)
                .map(p -> toCafeDTO(p, null, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void toggleLike(UUID cafeId, UUID userId) {
        Partner partner = partnerRepository.findById(cafeId)
                .orElseThrow(() -> new ResourceNotFoundException("Cafe not found: " + cafeId));
        if (partner.getLikes() == null) partner.setLikes(0);
        partner.setLikes(partner.getLikes() + 1);
        partnerRepository.save(partner);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VenueDTO> getVenueMarkers(Double lat, Double lng, UUID userId) {
        return partnerRepository.findAll().stream()
                .filter(p -> p.getIsActive())
                .map(p -> toVenueDTO(p, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VenueDTO getVenueDetail(UUID id, UUID userId) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found: " + id));
        return toVenueDTO(partner, userId);
    }


    @Override
    @Transactional
    public void toggleFavorite(UUID venueId, UUID userId) {
        Optional<UserFavoriteVenue> existing = favoriteRepository.findByUserIdAndPartnerId(userId, venueId);
        if (existing.isPresent()) {
            UserFavoriteVenue fav = existing.get();
            fav.setIsActive(!fav.getIsActive());
            favoriteRepository.save(fav);
        } else {
            Partner partner = partnerRepository.findById(venueId)
                    .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            UserFavoriteVenue fav = UserFavoriteVenue.builder()
                    .user(user)
                    .partner(partner)
                    .build();
            fav.setIsActive(true);
            favoriteRepository.save(fav);
        }
    }

    // === Helpers ===

    private CafeDTO toCafeDTO(Partner partner, Double userLat, Double userLng) {
        Location location = locationRepository.findAll().stream()
                .filter(l -> l.getPartner().getId().equals(partner.getId()))
                .findFirst().orElse(null);

        Double lat = location != null ? location.getLatitude() : 0.0;
        Double lng = location != null ? location.getLongitude() : 0.0;
        Double distance = (userLat != null && userLng != null) ? calculateDistance(userLat, userLng, lat, lng) : null;

        return CafeDTO.builder()
                .id(partner.getId())
                .name(partner.getName())
                .description(partner.getDescription())
                .image(partner.getImageUrl())
                .lat(lat)
                .lng(lng)
                .distance(distance)
                .rating(partner.getRating())
                .likes(partner.getLikes() != null ? partner.getLikes() : 0)
                .reviews(partner.getReviews() != null ? partner.getReviews() : 0)
                .isTrending(partner.getIsTrending() != null && partner.getIsTrending())
                .address(location != null ? location.getAddress() : "")
                .phone(partner.getPhone())
                .isOpen(partner.getIsOpen() != null && partner.getIsOpen())
                .price(partner.getPrice())
                .build();
    }

    private VenueDTO toVenueDTO(Partner partner, UUID userId) {
        Location location = locationRepository.findAll().stream()
                .filter(l -> l.getPartner().getId().equals(partner.getId()))
                .findFirst().orElse(null);

        boolean isFavorite = userId != null && favoriteRepository.existsByUserIdAndPartnerIdAndIsActiveTrue(userId, partner.getId());

        String markerType = "cafe";
        if (partner.getIsTrending() != null && partner.getIsTrending()) markerType = "trending";
        if (isFavorite) markerType = "favorite";

        return VenueDTO.builder()
                .id(partner.getId())
                .name(partner.getName())
                .imageUrl(partner.getImageUrl())
                .rating(partner.getRating())
                .position(VenueDTO.PositionDTO.builder()
                        .lat(location != null ? location.getLatitude() : 0.0)
                        .lng(location != null ? location.getLongitude() : 0.0)
                        .build())
                .markerType(markerType)
                .isFavorite(isFavorite)
                .build();
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371000; // meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
