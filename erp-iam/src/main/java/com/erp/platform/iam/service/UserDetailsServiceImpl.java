package com.erp.platform.iam.service;

import com.erp.platform.core.security.UserPrincipal;
import com.erp.platform.iam.entity.Collaborator;
import com.erp.platform.iam.enums.CollaboratorStatus;
import com.erp.platform.iam.repository.CollaboratorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final CollaboratorRepository collaboratorRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Collaborator collab = collaboratorRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur non trouvé: " + email));

        return new UserPrincipal(
                collab.getId(),
                collab.getEmail(),
                collab.getPasswordHash(),
                collab.getRole().name(),
                collab.getPartner() != null ? collab.getPartner().getId() : null,
                collab.getPartner() != null ? collab.getPartner().getCode() : null,
                collab.getDefaultLocal() != null ? collab.getDefaultLocal().getId() : null,
                collab.getLocalAccess() != null
                        ? collab.getLocalAccess().stream().map(id -> (UUID) id).toList()
                        : null,
                collab.getStatus() == CollaboratorStatus.ACTIVE
        );
    }
}
