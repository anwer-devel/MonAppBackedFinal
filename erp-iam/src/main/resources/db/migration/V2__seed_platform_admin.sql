-- V2__seed_platform_admin.sql
-- Seed initial PLATFORM_ADMIN user
-- Email: admin@1minuterp.com
-- Password: Admin@123456 (BCrypt hash with strength 12)

INSERT INTO collaborators (
    id, email, password_hash, first_name, last_name,
    role, status, created_at
) VALUES (
    gen_random_uuid(),
    'admin@1minuterp.com',
    '$2a$12$LJ3m4ys3GOXQ8IkVNFOjruYaFvZpRRZR8sTxL7WhIczfGgNVcthOW',
    'Platform',
    'Admin',
    'PLATFORM_ADMIN',
    'ACTIVE',
    NOW()
) ON CONFLICT (email) DO NOTHING;
