package com.app.coupon.repository;

import com.app.coupon.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, UUID> {
    List<UserCoupon> findByUserIdAndIsActiveTrue(UUID userId);
    boolean existsByUserIdAndCouponIdAndIsActiveTrue(UUID userId, UUID couponId);
}
