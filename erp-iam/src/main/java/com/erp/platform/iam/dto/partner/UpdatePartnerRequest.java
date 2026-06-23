package com.erp.platform.iam.dto.partner;

import com.erp.platform.iam.enums.PlanType;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdatePartnerRequest {

    private String name;
    private String phone;
    private String address;
    private String taxNumber;
    private PlanType plan;

    @FutureOrPresent(message = "La date de fin d'abonnement doit être dans le futur")
    private LocalDate subscriptionEnd;
}
