package com.app.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponDTO {
    private UUID id;
    private String title;
    private String description;
    private Integer discountPercent;
    private Double discountAmount;
    private String imageUrl;
    private UUID partnerId;
    private String partnerName;
    private LocalDateTime expiresAt;
    private String status;
    private Boolean isOwned;
}
