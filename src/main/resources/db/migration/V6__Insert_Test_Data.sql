-- Insert test admin user (password: AdminPassword123)
INSERT INTO users (id, email, password, role, email_verified, is_active, created_at, updated_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440001'::uuid,
    'admin@example.com',
    '$2a$12$abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzab',
    'ADMIN',
    true,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- Insert test partner owner (password: PartnerPassword123)
INSERT INTO users (id, email, password, role, email_verified, is_active, created_at, updated_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440002'::uuid,
    'partner@example.com',
    '$2a$12$bcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabc',
    'PARTNER_OWNER',
    true,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- Insert test regular user (password: UserPassword123)
INSERT INTO users (id, email, password, role, email_verified, is_active, created_at, updated_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440003'::uuid,
    'user@example.com',
    '$2a$12$cdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcd',
    'USER',
    true,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- Insert test zones
INSERT INTO zones (id, name, description, latitude, longitude, partner_count, is_active, created_at, updated_at)
VALUES
    ('550e8400-e29b-41d4-a716-446655440010'::uuid, 'Downtown Paris', 'Central business district', 48.8566, 2.3522, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440011'::uuid, 'La Défense', 'Modern business district', 48.8950, 2.2361, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440012'::uuid, 'Marais', 'Historic district', 48.8610, 2.3645, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- Insert test partners
INSERT INTO partners (id, name, description, type, rating, is_verified, zone_id, owner_id, asset_count, is_active, created_at, updated_at)
VALUES
    ('550e8400-e29b-41d4-a716-446655440020'::uuid, 'Café Central', 'Best coffee in Paris', 'CAFE', 4.8, true, '550e8400-e29b-41d4-a716-446655440010'::uuid, '550e8400-e29b-41d4-a716-446655440002'::uuid, 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440021'::uuid, 'La Brasserie', 'Traditional French restaurant', 'RESTAURANT', 4.6, true, '550e8400-e29b-41d4-a716-446655440010'::uuid, '550e8400-e29b-41d4-a716-446655440002'::uuid, 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Update zone partner counts
UPDATE zones SET partner_count = 2 WHERE id = '550e8400-e29b-41d4-a716-446655440010'::uuid;

-- Insert test locations
INSERT INTO locations (id, partner_id, latitude, longitude, address, city, country, is_active, created_at, updated_at)
VALUES
    ('550e8400-e29b-41d4-a716-446655440030'::uuid, '550e8400-e29b-41d4-a716-446655440020'::uuid, 48.8575, 2.2959, '123 Rue de Rivoli', 'Paris', 'France', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440031'::uuid, '550e8400-e29b-41d4-a716-446655440021'::uuid, 48.8600, 2.2965, '456 Rue Saint-Antoine', 'Paris', 'France', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

