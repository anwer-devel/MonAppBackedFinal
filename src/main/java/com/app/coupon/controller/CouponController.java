package com.app.coupon.controller;

import com.app.common.response.ApiResponse;
import com.app.common.security.SecurityUtil;
import com.app.coupon.dto.CouponDTO;
import com.app.coupon.dto.CreateCouponRequest;
import com.app.coupon.dto.UpdateCouponRequest;
import com.app.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Coupons", description = "Coupon management endpoints")
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    @Operation(summary = "Available coupons", description = "Get coupons available based on user XP")
    public ResponseEntity<ApiResponse<List<CouponDTO>>> getAvailableCoupons() {
        UUID userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(couponService.getAvailableCoupons(userId), "Coupons retrieved"));
    }

    @GetMapping("/mine")
    @Operation(summary = "My coupons", description = "Get user's claimed coupons")
    public ResponseEntity<ApiResponse<List<CouponDTO>>> getMyCoupons() {
        UUID userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(couponService.getMyCoupons(userId), "My coupons retrieved"));
    }

    @PostMapping("/{couponId}/claim")
    @Operation(summary = "Claim coupon")
    public ResponseEntity<ApiResponse<CouponDTO>> claimCoupon(@PathVariable UUID couponId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        CouponDTO result = couponService.claimCoupon(userId, couponId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Coupon claimed"));
    }

    @PostMapping("/{userCouponId}/use")
    @Operation(summary = "Use coupon")
    public ResponseEntity<ApiResponse<Void>> useCoupon(@PathVariable UUID userCouponId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        couponService.useCoupon(userId, userCouponId);
        return ResponseEntity.ok(ApiResponse.success(null, "Coupon used"));
    }

    // === ADMIN ENDPOINTS ===

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all coupons (Admin Only)", description = "Retrieve all coupons with pagination")
    public ResponseEntity<ApiResponse<Page<CouponDTO>>> getAllCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CouponDTO> result = couponService.getAllCoupons(pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "Coupons retrieved"));
    }

    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create coupon (Admin Only)", description = "Create a new coupon")
    public ResponseEntity<ApiResponse<CouponDTO>> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        CouponDTO result = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Coupon created successfully"));
    }

    @PutMapping("/admin/{couponId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update coupon (Admin Only)", description = "Update an existing coupon")
    public ResponseEntity<ApiResponse<CouponDTO>> updateCoupon(
            @PathVariable UUID couponId,
            @Valid @RequestBody UpdateCouponRequest request) {
        CouponDTO result = couponService.updateCoupon(couponId, request);
        return ResponseEntity.ok(ApiResponse.success(result, "Coupon updated successfully"));
    }

    @DeleteMapping("/admin/{couponId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete coupon (Admin Only)", description = "Delete (soft delete) a coupon")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable UUID couponId) {
        couponService.deleteCoupon(couponId);
        return ResponseEntity.ok(ApiResponse.success(null, "Coupon deleted successfully"));
    }
}
