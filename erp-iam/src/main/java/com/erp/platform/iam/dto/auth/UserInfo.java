package com.erp.platform.iam.dto.auth;

import com.erp.platform.iam.entity.PartnerConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private UUID partnerId;
    private String partnerCode;
    private String partnerName;
    private UUID defaultLocalId;
    private String defaultLocalCode;
    private String defaultLocalName;
    private List<UUID> localAccess;
    private PartnerConfig partnerConfig;
}
