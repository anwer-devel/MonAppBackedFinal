-- V4__seed_default_catalog.sql

INSERT INTO units (code, name, symbol, type, is_default) VALUES
('PCS', 'Pièce', 'pcs', 'UNIT', TRUE),
('KG', 'Kilogramme', 'kg', 'WEIGHT', FALSE),
('L', 'Litre', 'L', 'VOLUME', FALSE),
('M', 'Mètre', 'm', 'LENGTH', FALSE),
('BOX', 'Boîte', 'box', 'UNIT', FALSE),
('PAIR', 'Paire', 'pr', 'UNIT', FALSE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO categories (code, name, applicable_sector, display_order) VALUES
('AUTO-MOTEUR', 'Moteur & Pièces moteur', 'AUTO', 10),
('AUTO-FREINAGE', 'Freinage', 'AUTO', 20),
('AUTO-FILTRES', 'Filtres auto', 'AUTO', 30),
('AUTO-SUSPENSION', 'Suspension & Direction', 'AUTO', 40),
('AUTO-ELECTRICITE', 'Électricité & Batterie', 'AUTO', 50),

('PHARMA-MEDICAMENTS', 'Médicaments', 'PHARMA', 10),
('PHARMA-PARA', 'Parapharmacie', 'PHARMA', 20),
('PHARMA-COSMETIQUE', 'Dermo-Cosmétique', 'PHARMA', 30),
('PHARMA-MATERIEL', 'Matériel Médical', 'PHARMA', 40),
('PHARMA-COMPLEMENTS', 'Compléments Alimentaires', 'PHARMA', 50),

('HARDWARE-OUTILLAGE', 'Outillage à main et électroportatif', 'HARDWARE', 10),
('HARDWARE-PLOMBERIE', 'Plomberie & Sanitaire', 'HARDWARE', 20),
('HARDWARE-ELECTRICITE', 'Électricité & Éclairage', 'HARDWARE', 30),
('HARDWARE-QUINCAILLERIE', 'Quincaillerie & Fixations', 'HARDWARE', 40),
('HARDWARE-PEINTURE', 'Peinture & Droguerie', 'HARDWARE', 50),

('MIXED-GENERAL', 'Catalogue Général', 'MIXED', 10),
('MIXED-DIVERS', 'Articles Divers', 'MIXED', 20)
ON CONFLICT (code) DO NOTHING;
