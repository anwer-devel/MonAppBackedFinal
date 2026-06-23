package com.erp.platform.iam.dto.partner;

import com.erp.platform.iam.entity.PartnerConfig;
import com.erp.platform.iam.enums.PartnerStatus;
import com.erp.platform.iam.enums.PlanType;
import com.erp.platform.iam.enums.SectorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerResponse {

    private UUID id;
    private String code;
    private String name;
    private SectorType sectorType;
    private PlanType plan;
    private PartnerStatus status;
    private String email;
    private String phone;
    private String address;
    private String taxNumber;
    private LocalDate subscriptionEnd;
    private String currency;
    private PartnerConfig config;
    private int localsCount;
    private int collaboratorsCount;
    private BigDecimal monthlyRevenue;
    private LocalDateTime createdAt;
}
