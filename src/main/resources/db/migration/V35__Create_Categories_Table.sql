-- Create categories table
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    cover_image VARCHAR(500),
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    visibility VARCHAR(20) NOT NULL,
    created_by VARCHAR(20) NOT NULL,
    partner_id UUID,
    tags TEXT[],
    metadata JSONB NOT NULL DEFAULT '{}',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_category_partner ON categories(partner_id);
CREATE INDEX idx_category_status ON categories(status);
CREATE INDEX idx_category_type ON categories(type);
CREATE INDEX idx_category_visibility ON categories(visibility);
CREATE INDEX idx_category_created_by ON categories(created_by);
CREATE INDEX idx_category_active ON categories(is_active);
CREATE INDEX idx_category_status_visibility ON categories(status, visibility);

-- Create trigger for categories
CREATE TRIGGER categories_update_updated_at BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

