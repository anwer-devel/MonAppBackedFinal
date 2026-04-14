CREATE TABLE partners (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    rating DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    zone_id UUID NOT NULL REFERENCES zones(id),
    owner_id UUID NOT NULL REFERENCES users(id),
    asset_count INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_partner_zone ON partners(zone_id);
CREATE INDEX IF NOT EXISTS idx_partner_owner ON partners(owner_id);
CREATE INDEX IF NOT EXISTS idx_partner_name ON partners(name);
CREATE INDEX IF NOT EXISTS idx_partner_active ON partners(is_active);

CREATE TRIGGER partners_update_updated_at BEFORE UPDATE ON partners
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

