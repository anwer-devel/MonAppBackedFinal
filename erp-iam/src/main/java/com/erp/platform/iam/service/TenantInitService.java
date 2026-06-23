package com.erp.platform.iam.service;

import com.erp.platform.core.tenant.TenantSchemaResolver;
import com.erp.platform.iam.entity.Partner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantInitService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates the tenant schema and initializes base tables for the partner.
     * In production, Flyway migrations would be run on the new schema.
     */
    public void initializeTenant(Partner partner) {
        String schema = TenantSchemaResolver.resolveSchema(partner.getCode());

        log.info("Initializing tenant schema: {}", schema);

        // Create the schema
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS \"" + schema + "\"");

        // Create base catalogue tables in the tenant schema
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS \"" + schema + "\".categories (" +
                "id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "code VARCHAR(50) NOT NULL UNIQUE, " +
                "name VARCHAR(200) NOT NULL, " +
                "parent_id UUID REFERENCES \"" + schema + "\".categories(id), " +
                "created_at TIMESTAMP DEFAULT NOW(), " +
                "is_deleted BOOLEAN DEFAULT FALSE)");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS \"" + schema + "\".units (" +
                "id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "code VARCHAR(20) NOT NULL UNIQUE, " +
                "name VARCHAR(100) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT NOW(), " +
                "is_deleted BOOLEAN DEFAULT FALSE)");

        // Seed default units
        jdbcTemplate.execute("INSERT INTO \"" + schema + "\".units (code, name) VALUES " +
                "('PCS', 'Pièce'), ('KG', 'Kilogramme'), ('L', 'Litre'), " +
                "('M', 'Mètre'), ('BOX', 'Boîte') " +
                "ON CONFLICT (code) DO NOTHING");

        // Seed categories based on sector type
        seedCategories(schema, partner);

        log.info("Tenant schema {} initialized successfully", schema);
    }

    private void seedCategories(String schema, Partner partner) {
        switch (partner.getSectorType()) {
            case AUTO -> {
                jdbcTemplate.execute("INSERT INTO \"" + schema + "\".categories (code, name) VALUES " +
                        "('PIECES-AUTO', 'Pièces automobiles'), " +
                        "('HUILES', 'Huiles et lubrifiants'), " +
                        "('FILTRES', 'Filtres'), " +
                        "('FREINS', 'Freins'), " +
                        "('ELECTRICITE', 'Électricité auto') " +
                        "ON CONFLICT (code) DO NOTHING");
            }
            case PHARMA -> {
                jdbcTemplate.execute("INSERT INTO \"" + schema + "\".categories (code, name) VALUES " +
                        "('MEDICAMENTS', 'Médicaments'), " +
                        "('PARA', 'Parapharmacie'), " +
                        "('COSMETIQUES', 'Cosmétiques'), " +
                        "('MATERIEL', 'Matériel médical'), " +
                        "('COMPLEMENTS', 'Compléments alimentaires') " +
                        "ON CONFLICT (code) DO NOTHING");
            }
            case HARDWARE -> {
                jdbcTemplate.execute("INSERT INTO \"" + schema + "\".categories (code, name) VALUES " +
                        "('OUTILLAGE', 'Outillage'), " +
                        "('PLOMBERIE', 'Plomberie'), " +
                        "('ELECTRICITE', 'Électricité'), " +
                        "('PEINTURE', 'Peinture'), " +
                        "('QUINCAILLERIE', 'Quincaillerie') " +
                        "ON CONFLICT (code) DO NOTHING");
            }
            case MIXED -> {
                jdbcTemplate.execute("INSERT INTO \"" + schema + "\".categories (code, name) VALUES " +
                        "('GENERAL', 'Général'), " +
                        "('DIVERS', 'Divers') " +
                        "ON CONFLICT (code) DO NOTHING");
            }
        }
    }
}
