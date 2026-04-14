package com.app.partner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueDTO {
    private UUID id;
    private String name;
    private String imageUrl;
    private Double rating;
    private Integer distanceMeters;
    private PositionDTO position;
    private String markerType; // cafe, challenge, trending, favorite, bonus
    private Boolean isFavorite;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionDTO {
        private Double lat;
        private Double lng;
    }
}
