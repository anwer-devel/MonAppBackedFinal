package com.erp.platform.catalog.dto.request;

import com.erp.platform.catalog.enums.GalenicForm;
import com.erp.platform.catalog.enums.StorageTemp;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PharmaExtensionRequest {
    private String dci;
    private String dosage;
    private GalenicForm galenic;
    private String therapeuticClass;
    private String ammNumber;
    private boolean requiresPrescription;
    private boolean isGeneric;
    private String princepsRef;
    private String laboratoryName;
    private StorageTemp storageTemp;
    private Integer shelfLifeDays;
}
