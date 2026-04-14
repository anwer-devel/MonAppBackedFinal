package com.app.coupon.entity;

import com.app.common.entity.BaseEntity;
import com.app.partner.entity.Partner;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "discount_percent")
    private Integer discountPercent;

    @Column(name = "discount_amount")
    private Double discountAmount;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Builder.Default
    @Column(name = "min_xp_required")
    private Integer minXpRequired = 0;

    @Builder.Default
    @Column(name = "max_uses")
    private Integer maxUses = 100;

    @Builder.Default
    @Column(name = "current_uses")
    private Integer currentUses = 0;
}
