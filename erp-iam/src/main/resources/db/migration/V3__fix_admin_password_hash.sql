-- V3__fix_admin_password_hash.sql
-- Fix BCrypt hash for admin@1minuterp.com
-- Password: Admin@123456 (BCrypt hash with strength 12)

UPDATE collaborators
SET password_hash = '$2a$12$Mu4upYXCz81q5yOrM60QAetsUImIUFk3IpJO86LOVhkJnyh37uhhG'
WHERE email = 'admin@1minuterp.com';
