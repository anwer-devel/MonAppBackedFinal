package com.app.category.dto;

import com.app.category.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for approving or rejecting a category")
public class ApproveCategoryDto {

    @NotNull(message = "Status is required")
    @Schema(description = "New status", example = "APPROVED")
    private Category.CategoryStatus status;

    @Schema(description = "Rejection reason (required if status is REJECTED)")
    private String rejectionReason;
}

