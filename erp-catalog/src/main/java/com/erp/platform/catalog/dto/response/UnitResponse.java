package com.erp.platform.catalog.dto.response;

import lombok.*;

import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UnitResponse {
    private UUID id;
    private String code;
    private String name;
    private String symbol;
    private String type;
    private boolean isDefault;
}
