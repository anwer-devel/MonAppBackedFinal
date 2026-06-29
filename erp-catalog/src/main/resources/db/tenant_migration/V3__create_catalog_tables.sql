-- V3__create_catalog_tables.sql

CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    parent_id UUID REFERENCES categories(id),
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    icon_name VARCHAR(50),
    color_hex VARCHAR(7),
    applicable_sector VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS units (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    type VARCHAR(20) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ref VARCHAR(100) NOT NULL UNIQUE,
    barcode VARCHAR(50) UNIQUE,
    name VARCHAR(300) NOT NULL,
    description TEXT,
    short_description VARCHAR(500),
    category_id UUID REFERENCES categories(id),
    unit_id UUID REFERENCES units(id),
    sector_type VARCHAR(20),
    purchase_price_ht NUMERIC(12, 3) NOT NULL DEFAULT 0,
    sale_price_ht NUMERIC(12, 3) NOT NULL,
    tax_rate NUMERIC(5, 2) NOT NULL DEFAULT 19.0,
    margin_rate NUMERIC(6, 2),
    min_stock_level INT NOT NULL DEFAULT 0,
    safety_stock_level INT NOT NULL DEFAULT 0,
    max_stock_level INT NOT NULL DEFAULT 9999,
    track_stock BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    attributes JSONB NOT NULL DEFAULT '{}'::jsonb,
    image_urls TEXT[],
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_products_ref ON products(ref);
CREATE INDEX IF NOT EXISTS idx_products_barcode ON products(barcode);
CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);

CREATE TABLE IF NOT EXISTS auto_part_extensions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL UNIQUE REFERENCES products(id) ON DELETE CASCADE,
    oem_ref VARCHAR(100),
    aftermarket_ref VARCHAR(100),
    brand VARCHAR(100),
    family_enum VARCHAR(30),
    technical_note TEXT,
    data_sheet_url VARCHAR(500),
    is_oem_equivalent BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_auto_part_oem_ref ON auto_part_extensions(oem_ref);

CREATE TABLE IF NOT EXISTS vehicle_compatibilities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auto_part_id UUID NOT NULL REFERENCES auto_part_extensions(id) ON DELETE CASCADE,
    vehicle_make VARCHAR(100) NOT NULL,
    vehicle_model VARCHAR(100) NOT NULL,
    vehicle_variant VARCHAR(100),
    year_from INT,
    year_to INT,
    engine_type VARCHAR(20),
    vin_pattern VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_vehicle_compat_vin ON vehicle_compatibilities(vin_pattern);

CREATE TABLE IF NOT EXISTS pharma_extensions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL UNIQUE REFERENCES products(id) ON DELETE CASCADE,
    dci VARCHAR(200),
    dosage VARCHAR(100),
    galenic VARCHAR(50),
    therapeutic_class VARCHAR(200),
    amm_number VARCHAR(100),
    requires_prescription BOOLEAN NOT NULL DEFAULT FALSE,
    is_generic BOOLEAN NOT NULL DEFAULT FALSE,
    princeps_ref VARCHAR(100),
    laboratory_name VARCHAR(200),
    storage_temp VARCHAR(20) NOT NULL DEFAULT 'AMBIENT',
    shelf_life_days INT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS hardware_extensions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL UNIQUE REFERENCES products(id) ON DELETE CASCADE,
    family_enum VARCHAR(30),
    material VARCHAR(100),
    dimensions VARCHAR(100),
    norm VARCHAR(100),
    conditioning VARCHAR(30),
    conditioning_qty INT NOT NULL DEFAULT 1,
    is_professional BOOLEAN NOT NULL DEFAULT FALSE,
    color_or_finish VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);
