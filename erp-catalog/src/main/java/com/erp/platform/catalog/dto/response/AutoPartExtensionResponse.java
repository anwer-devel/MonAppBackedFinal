package com.erp.platform.catalog.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AutoPartExtensionResponse {
    private UUID id;
    private String oemRef;
    private String aftermarketRef;
    private String brand;
    private String familyEnum;
    private String technicalNote;
    private String dataSheetUrl;
    private boolean isOemEquivalent;
    private List<VehicleCompatibilityResponse> compatibilities;
}
