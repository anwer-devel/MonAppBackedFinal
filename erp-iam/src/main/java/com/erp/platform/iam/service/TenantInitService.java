package com.erp.platform.iam.service;

import com.erp.platform.core.tenant.TenantSchemaResolver;
import com.erp.platform.iam.entity.Partner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantInitService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates the tenant schema and runs Flyway migrations for the tenant.
     */
    public void initializeTenant(Partner partner) {
        String schema = TenantSchemaResolver.resolveSchema(partner.getCode());

        log.info("Initializing tenant schema: {}", schema);

        // Create the schema
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS \"" + schema + "\"");

        // Run Flyway migrations on the tenant schema
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource != null) {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(schema)
                    .locations("classpath:db/tenant_migration")
                    .baselineOnMigrate(true)
                    .baselineVersion("0")
                    .load();
            flyway.migrate();
        } else {
            log.error("DataSource is null, unable to execute tenant migrations for schema {}", schema);
        }

        log.info("Tenant schema {} initialized successfully", schema);
    }
}
