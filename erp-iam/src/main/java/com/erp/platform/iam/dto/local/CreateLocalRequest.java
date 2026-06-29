package com.erp.platform.iam.dto.local;

import com.erp.platform.iam.enums.LocalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateLocalRequest {

    private UUID partnerId; // Optionnel : ignoré si PARTNER_ADMIN

    @NotBlank(message = "Le code est requis")
    @Size(max = 50, message = "Le code ne peut pas dépasser 50 caractères")
    private String code;

    @NotBlank(message = "Le nom est requis")
    @Size(max = 200, message = "Le nom ne peut pas dépasser 200 caractères")
    private String name;

    @NotNull(message = "Le type est requis")
    private LocalType type;

    private String address;
    private String phone;
    private String email;
    private boolean isMain;
}
