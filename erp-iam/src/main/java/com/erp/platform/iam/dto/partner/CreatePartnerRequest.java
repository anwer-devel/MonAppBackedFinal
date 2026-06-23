package com.erp.platform.iam.dto.partner;

import com.erp.platform.iam.enums.PlanType;
import com.erp.platform.iam.enums.SectorType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreatePartnerRequest {

    @NotBlank(message = "Le code est requis")
    @Size(min = 2, max = 20, message = "Le code doit contenir entre 2 et 20 caractères")
    @Pattern(regexp = "[A-Z0-9\\-]+", message = "Le code ne peut contenir que A-Z, 0-9 et -")
    private String code;

    @NotBlank(message = "Le nom est requis")
    @Size(max = 200, message = "Le nom ne peut pas dépasser 200 caractères")
    private String name;

    @NotNull(message = "Le secteur est requis")
    private SectorType sectorType;

    @NotNull(message = "Le plan est requis")
    private PlanType plan;

    @NotBlank(message = "L'email est requis")
    @Email(message = "Format email invalide")
    private String email;

    private String phone;
    private String address;
    private String taxNumber;

    @FutureOrPresent(message = "La date de fin d'abonnement doit être dans le futur")
    private LocalDate subscriptionEnd;
}
