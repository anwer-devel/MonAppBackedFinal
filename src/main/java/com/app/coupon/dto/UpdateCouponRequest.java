package com.app.coupon.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCouponRequest {
    
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;
    
    private String description;
    
    @Min(value = 1, message = "Discount percent must be at least 1")
    @Max(value = 100, message = "Discount percent cannot exceed 100")
    private Integer discountPercent;
    
    private Double discountAmount;
    
    private String imageUrl;
    
    @FutureOrPresent(message = "Expiry date must be in the future")
    private LocalDateTime expiresAt;
    
    private String status;  // AVAILABLE, CLAIMED, USED
}
