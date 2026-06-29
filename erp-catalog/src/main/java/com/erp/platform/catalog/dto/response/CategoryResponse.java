package com.erp.platform.catalog.dto.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CategoryResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private UUID parentId;
    private String parentName;
    private int displayOrder;
    private boolean isActive;
    private String iconName;
    private String colorHex;
    private String applicableSector;
    private int productCount;
    @Builder.Default
    private List<CategoryResponse> children = new ArrayList<>();
}
