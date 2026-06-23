package com.erp.platform.iam.dto.partner;

import com.erp.platform.iam.enums.PartnerStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePartnerStatusRequest {

    @NotNull(message = "Le statut est requis")
    private PartnerStatus status;
}
