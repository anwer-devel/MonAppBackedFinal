package com.erp.platform.iam.dto.collaborator;

import com.erp.platform.iam.enums.CollaboratorRole;
import com.erp.platform.iam.enums.CollaboratorStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollaboratorResponse {

    private UUID id;
    private UUID partnerId;
    private UUID defaultLocalId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String defaultLocalName;
    private String partnerName;
    private CollaboratorRole role;
    private CollaboratorStatus status;
    private boolean multiLocal;
    private List<UUID> localAccess;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
