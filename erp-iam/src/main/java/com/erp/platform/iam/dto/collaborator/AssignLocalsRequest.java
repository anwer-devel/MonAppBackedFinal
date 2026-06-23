package com.erp.platform.iam.dto.collaborator;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AssignLocalsRequest {

    @NotEmpty(message = "La liste des locaux est requise")
    private List<UUID> localAccess;
}
