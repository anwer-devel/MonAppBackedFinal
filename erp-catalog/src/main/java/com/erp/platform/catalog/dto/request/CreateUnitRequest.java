package com.erp.platform.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateUnitRequest {
    @NotBlank
    @Size(max = 20)
    private String code;
    @NotBlank
    private String name;
    @NotBlank
    private String symbol;
    @NotNull
    private String type;
}
