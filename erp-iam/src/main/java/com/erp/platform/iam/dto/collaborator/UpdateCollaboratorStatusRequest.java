package com.erp.platform.iam.dto.collaborator;

import com.erp.platform.iam.enums.CollaboratorStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCollaboratorStatusRequest {

    @NotNull(message = "Le statut est requis")
    private CollaboratorStatus status;
}
