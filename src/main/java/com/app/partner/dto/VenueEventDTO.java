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
public class VenueEventDTO {
    private UUID id;
    private String title;
    private String imageUrl;
    private Integer playerCount;
    private Boolean isLive;
}
