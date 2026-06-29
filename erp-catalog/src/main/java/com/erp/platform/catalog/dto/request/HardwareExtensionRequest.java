package com.erp.platform.catalog.dto.request;

import com.erp.platform.catalog.enums.HardwareFamily;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class HardwareExtensionRequest {
    private HardwareFamily familyEnum;
    private String material;
    private String dimensions;
    private String norm;
    private String conditioning;
    private int conditioningQty;
    private boolean isProfessional;
    private String colorOrFinish;
}
