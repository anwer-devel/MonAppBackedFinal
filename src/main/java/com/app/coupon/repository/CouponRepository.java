package com.app.coupon.repository;

import com.app.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    List<Coupon> findByPartnerIdAndIsActiveTrue(UUID partnerId);
    List<Coupon> findByIsActiveTrue();
}
