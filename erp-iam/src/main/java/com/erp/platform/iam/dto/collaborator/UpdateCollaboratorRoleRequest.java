package com.erp.platform.iam.dto.collaborator;

import com.erp.platform.iam.enums.CollaboratorRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCollaboratorRoleRequest {

    @NotNull(message = "Le rôle est requis")
    private CollaboratorRole role;
}
