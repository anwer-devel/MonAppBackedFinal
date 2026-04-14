package com.app.coupon.service.impl;

import com.app.auth.entity.User;
import com.app.auth.repository.UserRepository;
import com.app.common.exception.BadRequestException;
import com.app.common.exception.ResourceNotFoundException;
import com.app.coupon.dto.CouponDTO;
import com.app.coupon.dto.CreateCouponRequest;
import com.app.coupon.dto.UpdateCouponRequest;
import com.app.coupon.entity.Coupon;
import com.app.coupon.entity.UserCoupon;
import com.app.coupon.repository.CouponRepository;
import com.app.coupon.repository.UserCouponRepository;
import com.app.coupon.service.CouponService;
import com.app.partner.entity.Partner;
import com.app.partner.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;
    private final PartnerRepository partnerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CouponDTO> getAvailableCoupons(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return couponRepository.findByIsActiveTrue().stream()
                .filter(c -> c.getMinXpRequired() <= user.getXp())
                .filter(c -> c.getCurrentUses() < c.getMaxUses())
                .filter(c -> c.getExpiresAt() == null || c.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(c -> toCouponDTO(c, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponDTO> getMyCoupons(UUID userId) {
        return userCouponRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(uc -> {
                    CouponDTO dto = toCouponDTO(uc.getCoupon(), userId);
                    dto.setStatus(uc.getStatus().name());
                    dto.setIsOwned(true);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CouponDTO claimCoupon(UUID userId, UUID couponId) {
        if (userCouponRepository.existsByUserIdAndCouponIdAndIsActiveTrue(userId, couponId)) {
            throw new BadRequestException("Coupon already claimed");
        }

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (coupon.getMinXpRequired() > user.getXp()) {
            throw new BadRequestException("Insufficient XP to claim this coupon");
        }

        UserCoupon userCoupon = UserCoupon.builder()
                .user(user)
                .coupon(coupon)
                .status(UserCoupon.CouponStatus.AVAILABLE)
                .build();
        userCoupon.setIsActive(true);
        userCouponRepository.save(userCoupon);

        coupon.setCurrentUses(coupon.getCurrentUses() + 1);
        couponRepository.save(coupon);

        user.setCouponsCount(user.getCouponsCount() + 1);
        userRepository.save(user);

        log.info("User {} claimed coupon {}", userId, couponId);

        CouponDTO dto = toCouponDTO(coupon, userId);
        dto.setIsOwned(true);
        return dto;
    }

    @Override
    @Transactional
    public void useCoupon(UUID userId, UUID userCouponId) {
        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new ResourceNotFoundException("User coupon not found"));

        if (!userCoupon.getUser().getId().equals(userId)) {
            throw new BadRequestException("Not your coupon");
        }

        userCoupon.setStatus(UserCoupon.CouponStatus.USED);
        userCoupon.setUsedAt(LocalDateTime.now());
        userCouponRepository.save(userCoupon);

        log.info("User {} used coupon {}", userId, userCouponId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponDTO> getAllCoupons(Pageable pageable) {
        return couponRepository.findAll(pageable)
                .map(c -> toCouponDTO(c, null));  // Pass null userId for admin view
    }

    @Override
    @Transactional
    public CouponDTO createCoupon(CreateCouponRequest request) {
        Partner partner = partnerRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id: " + request.getPartnerId()));

        Coupon coupon = Coupon.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .discountPercent(request.getDiscountPercent())
                .discountAmount(request.getDiscountAmount())
                .imageUrl(request.getImageUrl())
                .partner(partner)
                .expiresAt(request.getExpiresAt())
                .currentUses(0)
                .maxUses(1000)  // Default max uses
                .minXpRequired(0)  // No minimum XP by default
                .build();

        coupon = couponRepository.save(coupon);
        log.info("Coupon created: {} ({})", coupon.getId(), coupon.getTitle());

        return toCouponDTO(coupon, null);
    }

    @Override
    @Transactional
    public CouponDTO updateCoupon(UUID couponId, UpdateCouponRequest request) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + couponId));

        if (request.getTitle() != null) {
            coupon.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            coupon.setDescription(request.getDescription());
        }
        if (request.getDiscountPercent() != null) {
            coupon.setDiscountPercent(request.getDiscountPercent());
        }
        if (request.getDiscountAmount() != null) {
            coupon.setDiscountAmount(request.getDiscountAmount());
        }
        if (request.getImageUrl() != null) {
            coupon.setImageUrl(request.getImageUrl());
        }
        if (request.getExpiresAt() != null) {
            coupon.setExpiresAt(request.getExpiresAt());
        }
        if (request.getStatus() != null) {
            coupon.setIsActive("AVAILABLE".equalsIgnoreCase(request.getStatus()));
        }

        coupon = couponRepository.save(coupon);
        log.info("Coupon updated: {} ({})", coupon.getId(), coupon.getTitle());

        return toCouponDTO(coupon, null);
    }

    @Override
    @Transactional
    public void deleteCoupon(UUID couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + couponId));

        coupon.setIsActive(false);
        couponRepository.save(coupon);

        log.info("Coupon deleted (soft delete): {}", couponId);
    }

    private CouponDTO toCouponDTO(Coupon coupon, UUID userId) {
        return CouponDTO.builder()
                .id(coupon.getId())
                .title(coupon.getTitle())
                .description(coupon.getDescription())
                .discountPercent(coupon.getDiscountPercent())
                .discountAmount(coupon.getDiscountAmount())
                .imageUrl(coupon.getImageUrl())
                .partnerId(coupon.getPartner().getId())
                .partnerName(coupon.getPartner().getName())
                .expiresAt(coupon.getExpiresAt())
                .status("AVAILABLE")
                .isOwned(userCouponRepository.existsByUserIdAndCouponIdAndIsActiveTrue(userId, coupon.getId()))
                .build();
    }
}
