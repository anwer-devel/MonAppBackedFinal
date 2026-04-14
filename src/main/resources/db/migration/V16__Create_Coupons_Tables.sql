-- V16: Create coupons table
CREATE TABLE coupons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    discount_percent INTEGER,
    discount_amount DOUBLE PRECISION,
    image_url VARCHAR(500),
    partner_id UUID NOT NULL REFERENCES partners(id),
    expires_at TIMESTAMP,
    min_xp_required INTEGER DEFAULT 0,
    max_uses INTEGER DEFAULT 100,
    current_uses INTEGER DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- V16: Create user_coupons table
CREATE TABLE user_coupons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    coupon_id UUID NOT NULL REFERENCES coupons(id),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    used_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_user_coupon UNIQUE (user_id, coupon_id)
);

CREATE INDEX idx_coupon_partner ON coupons(partner_id);
CREATE INDEX idx_user_coupon_user ON user_coupons(user_id);
CREATE INDEX idx_user_coupon_status ON user_coupons(status);
