package com.erp.platform.core.tenant;

/**
 * Resolves tenant schema name from partner code.
 * Convention: schema = "tenant_" + partnerCode.toLowerCase().replaceAll("-", "_")
 */
public final class TenantSchemaResolver {

    private TenantSchemaResolver() {
    }

    public static String resolveSchema(String partnerCode) {
        if (partnerCode == null || partnerCode.isBlank()) {
            return "public";
        }
        return "tenant_" + partnerCode.toLowerCase().replace("-", "_");
    }
}
