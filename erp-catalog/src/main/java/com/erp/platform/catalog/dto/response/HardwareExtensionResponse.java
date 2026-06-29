package com.erp.platform.catalog.dto.response;

import lombok.*;

import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class HardwareExtensionResponse {
    private UUID id;
    private String familyEnum;
    private String material;
    private String dimensions;
    private String norm;
    private String conditioning;
    private int conditioningQty;
    private boolean isProfessional;
    private String colorOrFinish;
}
