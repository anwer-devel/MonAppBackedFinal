package com.app.coupon.dto;

import jakarta.validation.constraints.*;
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
public class CreateCouponRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;
    
    private String description;
    
    @Min(value = 1, message = "Discount percent must be at least 1")
    @Max(value = 100, message = "Discount percent cannot exceed 100")
    private Integer discountPercent;
    
    private Double discountAmount;
    
    private String imageUrl;
    
    @NotNull(message = "Partner ID is required")
    private UUID partnerId;
    
    @NotNull(message = "Expiry date is required")
    @FutureOrPresent(message = "Expiry date must be in the future")
    private LocalDateTime expiresAt;
    
    @NotBlank(message = "Status is required")
    private String status;  // AVAILABLE, CLAIMED, USED
}
