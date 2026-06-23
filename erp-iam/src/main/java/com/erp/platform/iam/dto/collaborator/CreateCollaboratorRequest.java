package com.erp.platform.iam.dto.collaborator;

import com.erp.platform.iam.enums.CollaboratorRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateCollaboratorRequest {

    @NotBlank(message = "Le prénom est requis")
    private String firstName;

    @NotBlank(message = "Le nom est requis")
    private String lastName;

    @NotBlank(message = "L'email est requis")
    @Email(message = "Format email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    private String phone;

    @NotNull(message = "Le rôle est requis")
    private CollaboratorRole role;

    private UUID defaultLocalId;

    private boolean multiLocal = false;

    private List<UUID> localAccess;
}
