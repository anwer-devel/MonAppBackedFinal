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
public class CafeDTO {
    private UUID id;
    private String name;
    private String description;
    private String image;
    private Double lat;
    private Double lng;
    private Double distance;
    private Double rating;
    private Integer likes;
    private Integer reviews;
    private Boolean isTrending;
    private String address;
    private String phone;
    private Boolean isOpen;
    private Double price;
}
