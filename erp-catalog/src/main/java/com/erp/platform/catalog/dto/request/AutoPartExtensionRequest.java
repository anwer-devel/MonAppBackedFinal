package com.erp.platform.catalog.dto.request;

import com.erp.platform.catalog.enums.AutoFamily;
import lombok.*;

import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AutoPartExtensionRequest {
    private String oemRef;
    private String aftermarketRef;
    private String brand;
    private AutoFamily familyEnum;
    private String technicalNote;
    private String dataSheetUrl;
    private boolean isOemEquivalent;
    private List<VehicleCompatibilityRequest> compatibilities;
}
