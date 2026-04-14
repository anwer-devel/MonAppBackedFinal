CREATE TABLE partner_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    partner_id UUID NOT NULL REFERENCES partners(id),
    type VARCHAR(50) NOT NULL,
    url VARCHAR(500) NOT NULL,
    metadata_json TEXT,
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_asset_partner ON partner_assets(partner_id);
CREATE INDEX IF NOT EXISTS idx_asset_type ON partner_assets(type);
CREATE INDEX IF NOT EXISTS idx_asset_active ON partner_assets(is_active);

CREATE TRIGGER partner_assets_update_updated_at BEFORE UPDATE ON partner_assets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

