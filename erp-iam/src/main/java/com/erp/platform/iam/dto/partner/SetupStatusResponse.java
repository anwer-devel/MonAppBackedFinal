package com.erp.platform.iam.dto.partner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetupStatusResponse {

    private boolean hasLocals;
    private boolean hasCollaborators;
}
