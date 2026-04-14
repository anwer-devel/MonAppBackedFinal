package com.app.coupon.service;

import com.app.coupon.dto.CouponDTO;
import com.app.coupon.dto.CreateCouponRequest;
import com.app.coupon.dto.UpdateCouponRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CouponService {
    // User endpoints
    List<CouponDTO> getAvailableCoupons(UUID userId);
    List<CouponDTO> getMyCoupons(UUID userId);
    CouponDTO claimCoupon(UUID userId, UUID couponId);
    void useCoupon(UUID userId, UUID userCouponId);
    
    // Admin endpoints
    Page<CouponDTO> getAllCoupons(Pageable pageable);
    CouponDTO createCoupon(CreateCouponRequest request);
    CouponDTO updateCoupon(UUID couponId, UpdateCouponRequest request);
    void deleteCoupon(UUID couponId);
}
