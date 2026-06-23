package com.erp.platform.iam.dto.partner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerStatsResponse {

    private long totalActive;
    private long totalTrial;
    private long totalSuspended;
    private BigDecimal totalMrr;
}
