package com.erp.platform.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateCategoryRequest {
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private String description;
    private UUID parentId;
    private int displayOrder;
    private String iconName;
    private String colorHex;
    private String applicableSector;
}
