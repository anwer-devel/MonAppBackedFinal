package com.erp.platform.catalog.dto.response;

import lombok.*;

import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PharmaExtensionResponse {
    private UUID id;
    private String dci;
    private String dosage;
    private String galenic;
    private String therapeuticClass;
    private String ammNumber;
    private boolean requiresPrescription;
    private boolean isGeneric;
    private String princepsRef;
    private String laboratoryName;
    private String storageTemp;
    private Integer shelfLifeDays;
}
