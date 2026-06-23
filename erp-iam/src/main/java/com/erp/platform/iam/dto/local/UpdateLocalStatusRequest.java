package com.erp.platform.iam.dto.local;

import com.erp.platform.iam.enums.LocalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateLocalStatusRequest {

    @NotNull(message = "Le statut est requis")
    private LocalStatus status;
}
