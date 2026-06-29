package com.erp.platform.iam.service;

import com.erp.platform.core.exception.ForbiddenException;
import com.erp.platform.core.exception.ResourceNotFoundException;
import com.erp.platform.iam.entity.Collaborator;
import com.erp.platform.iam.enums.CollaboratorRole;
import com.erp.platform.iam.repository.CollaboratorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionChecker {

    private final CollaboratorRepository collaboratorRepository;

    public void checkPartnerAccess(UUID userId, UUID requestedPartnerId) {
        Collaborator collab = collaboratorRepository.findByIdWithRelations(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator", "id", userId));

        if (collab.getRole() == CollaboratorRole.PLATFORM_ADMIN) {
            return; // Full access
        }

        if (collab.getRole() == CollaboratorRole.PARTNER_ADMIN
                && collab.getPartner() != null
                && collab.getPartner().getId().equals(requestedPartnerId)) {
            return; // Own partner access
        }

        throw new ForbiddenException("Accès non autorisé à ce partenaire");
    }

    public void checkLocalAccess(UUID userId, UUID requestedLocalId) {
        Collaborator collab = collaboratorRepository.findByIdWithRelations(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator", "id", userId));

        if (collab.getRole() == CollaboratorRole.PLATFORM_ADMIN
                || collab.getRole() == CollaboratorRole.PARTNER_ADMIN) {
            return;
        }

        // Operational roles: check local access
        if (collab.getDefaultLocal() != null
                && collab.getDefaultLocal().getId().equals(requestedLocalId)) {
            return;
        }

        if (collab.getLocalAccess() != null
                && collab.getLocalAccess().contains(requestedLocalId)) {
            return;
        }

        throw new ForbiddenException("Accès non autorisé à ce local");
    }

    public Collaborator getCollaborator(UUID userId) {
        return collaboratorRepository.findByIdWithRelations(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator", "id", userId));
    }
}
