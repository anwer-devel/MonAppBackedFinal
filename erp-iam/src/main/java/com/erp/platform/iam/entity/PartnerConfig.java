package com.erp.platform.iam.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerConfig implements Serializable {

    private boolean stockEnabled = true;
    private boolean posEnabled = true;
    private boolean billingEnabled = true;
    private boolean purchaseEnabled = false;
    private boolean reportsEnabled = false;
    private boolean multiWarehouseEnabled = false;
    private boolean vehicleCompatEnabled = false;
    private boolean vinLookupEnabled = false;
    private boolean expiryTrackingEnabled = false;
    private boolean lotTrackingEnabled = false;
    private boolean prescriptionEnabled = false;

    private String defaultCurrency = "TND";
    private BigDecimal defaultTaxRate = new BigDecimal("19.0");
    private String invoicePrefix = "FAC";
    private int invoiceStartNumber = 1000;
    private String stockValuationMethod = "AVERAGE_COST";
}
