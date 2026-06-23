package com.erp.platform.iam.dto.partner;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PartnerConfigRequest {

    private boolean stockEnabled;
    private boolean posEnabled;
    private boolean billingEnabled;
    private boolean purchaseEnabled;
    private boolean reportsEnabled;
    private boolean multiWarehouseEnabled;
    private boolean vehicleCompatEnabled;
    private boolean vinLookupEnabled;
    private boolean expiryTrackingEnabled;
    private boolean lotTrackingEnabled;
    private boolean prescriptionEnabled;

    @NotBlank(message = "La devise par défaut est requise")
    private String defaultCurrency;

    @DecimalMin(value = "0", message = "Le taux de taxe doit être >= 0")
    private BigDecimal defaultTaxRate;

    @NotBlank(message = "Le préfixe de facture est requis")
    private String invoicePrefix;

    @Min(value = 1, message = "Le numéro de départ doit être >= 1")
    private int invoiceStartNumber;

    @Pattern(regexp = "AVERAGE_COST|FIFO|FEFO",
            message = "La méthode de valorisation doit être AVERAGE_COST, FIFO ou FEFO")
    private String stockValuationMethod;
}
