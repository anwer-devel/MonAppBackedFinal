package com.erp.platform.iam.service;

import com.erp.platform.core.audit.AuditService;
import com.erp.platform.core.common.PageResponse;
import com.erp.platform.core.exception.ConflictException;
import com.erp.platform.core.exception.ForbiddenException;
import com.erp.platform.core.exception.ResourceNotFoundException;
import com.erp.platform.iam.dto.collaborator.*;
import com.erp.platform.iam.entity.Collaborator;
import com.erp.platform.iam.entity.LocalUnit;
import com.erp.platform.iam.entity.Partner;
import com.erp.platform.iam.enums.CollaboratorRole;
import com.erp.platform.iam.enums.CollaboratorStatus;
import com.erp.platform.iam.mapper.CollaboratorMapper;
import com.erp.platform.iam.repository.CollaboratorRepository;
import com.erp.platform.iam.repository.LocalUnitRepository;
import com.erp.platform.iam.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollaboratorService {

    private final CollaboratorRepository collaboratorRepository;
    private final PartnerRepository partnerRepository;
    private final LocalUnitRepository localUnitRepository;
    private final CollaboratorMapper collaboratorMapper;
    private final PermissionChecker permissionChecker;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%&*";
    private static final SecureRandom RANDOM = new SecureRandom();

    public PageResponse<CollaboratorResponse> getAll(UUID partnerId, CollaboratorRole role,
                                                      UUID localId, String q,
                                                      Pageable pageable,
                                                      UUID requestingUserId) {
        if (partnerId != null) {
            permissionChecker.checkPartnerAccess(requestingUserId, partnerId);
        }

        String qParam = (q != null && !q.isBlank()) ? q.trim() : null;
        String qPattern = (qParam != null) ? "%" + qParam.toLowerCase() + "%" : null;

        Page<Collaborator> page = collaboratorRepository.findWithFilters(
                partnerId, role, localId, qParam, qPattern, pageable);
        Page<CollaboratorResponse> responsePage = page.map(collaboratorMapper::toResponse);
        return PageResponse.from(responsePage);
    }

    public CollaboratorResponse getById(UUID id) {
        Collaborator collab = findCollabOrThrow(id);
        return collaboratorMapper.toResponse(collab);
    }

    @Transactional
    public CollaboratorResponse create(UUID partnerId, CreateCollaboratorRequest req,
                                        UUID createdByUserId) {
        permissionChecker.checkPartnerAccess(createdByUserId, partnerId);

        Partner partner = partnerRepository.findByIdAndIsDeletedFalse(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner", "id", partnerId));

        // Check email uniqueness
        collaboratorRepository.findByEmailAndIsDeletedFalse(req.getEmail())
                .ifPresent(c -> { throw new ConflictException("Cet email est déjà utilisé", "email"); });

        // Validate defaultLocalId for non PARTNER_ADMIN roles
        LocalUnit defaultLocal = null;
        if (req.getRole() != CollaboratorRole.PARTNER_ADMIN) {
            if (req.getDefaultLocalId() == null) {
                throw new IllegalArgumentException(
                        "Le local par défaut est requis pour le rôle " + req.getRole());
            }
            defaultLocal = localUnitRepository.findByIdAndIsDeletedFalse(req.getDefaultLocalId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "LocalUnit", "id", req.getDefaultLocalId()));
            if (!defaultLocal.getPartner().getId().equals(partnerId)) {
                throw new ForbiddenException("Ce local n'appartient pas à ce partenaire");
            }
        }

        // Validate multi-local access
        List<UUID> localAccess = null;
        if (req.isMultiLocal() && req.getLocalAccess() != null) {
            for (UUID localId : req.getLocalAccess()) {
                LocalUnit lu = localUnitRepository.findByIdAndIsDeletedFalse(localId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "LocalUnit", "id", localId));
                if (!lu.getPartner().getId().equals(partnerId)) {
                    throw new ForbiddenException(
                            "Le local " + localId + " n'appartient pas à ce partenaire");
                }
            }
            localAccess = req.getLocalAccess();
        }

        Collaborator collab = Collaborator.builder()
                .partner(partner)
                .defaultLocal(defaultLocal)
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .phone(req.getPhone())
                .role(req.getRole())
                .status(CollaboratorStatus.ACTIVE)
                .multiLocal(req.isMultiLocal())
                .localAccess(localAccess != null ? localAccess : List.of())
                .build();

        collab = collaboratorRepository.save(collab);

        auditService.log(createdByUserId, null, "COLLABORATOR_CREATED",
                "Collaborator", collab.getId(), null, null, partner.getCode(), null);

        return collaboratorMapper.toResponse(collab);
    }

    @Transactional
    public CollaboratorResponse update(UUID id, UpdateCollaboratorRequest req,
                                        UUID requestingUserId) {
        Collaborator collab = findCollabOrThrow(id);
        if (collab.getPartner() != null) {
            permissionChecker.checkPartnerAccess(requestingUserId, collab.getPartner().getId());
        }

        if (req.getFirstName() != null) collab.setFirstName(req.getFirstName());
        if (req.getLastName() != null) collab.setLastName(req.getLastName());
        if (req.getPhone() != null) collab.setPhone(req.getPhone());

        collab = collaboratorRepository.save(collab);
        return collaboratorMapper.toResponse(collab);
    }

    @Transactional
    public CollaboratorResponse updateRole(UUID id, CollaboratorRole role,
                                            UUID requestingUserId) {
        Collaborator collab = findCollabOrThrow(id);
        if (collab.getPartner() != null) {
            permissionChecker.checkPartnerAccess(requestingUserId, collab.getPartner().getId());
        }

        String oldRole = collab.getRole().name();
        collab.setRole(role);
        collab = collaboratorRepository.save(collab);

        auditService.log(requestingUserId, null, "COLLABORATOR_ROLE_CHANGED",
                "Collaborator", collab.getId(), oldRole, role.name(),
                collab.getPartner() != null ? collab.getPartner().getCode() : null, null);

        return collaboratorMapper.toResponse(collab);
    }

    @Transactional
    public CollaboratorResponse updateStatus(UUID id, CollaboratorStatus status,
                                              UUID requestingUserId) {
        Collaborator collab = findCollabOrThrow(id);
        if (collab.getPartner() != null) {
            permissionChecker.checkPartnerAccess(requestingUserId, collab.getPartner().getId());
        }

        collab.setStatus(status);
        collab = collaboratorRepository.save(collab);
        return collaboratorMapper.toResponse(collab);
    }

    @Transactional
    public CollaboratorResponse assignLocals(UUID id, List<UUID> localIds,
                                              UUID requestingUserId) {
        Collaborator collab = findCollabOrThrow(id);
        if (collab.getPartner() != null) {
            permissionChecker.checkPartnerAccess(requestingUserId, collab.getPartner().getId());

            // Validate all locals belong to the same partner
            for (UUID localId : localIds) {
                LocalUnit lu = localUnitRepository.findByIdAndIsDeletedFalse(localId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "LocalUnit", "id", localId));
                if (!lu.getPartner().getId().equals(collab.getPartner().getId())) {
                    throw new ForbiddenException(
                            "Le local " + localId + " n'appartient pas au partenaire");
                }
            }
        }

        collab.setMultiLocal(true);
        collab.setLocalAccess(localIds);
        collab = collaboratorRepository.save(collab);

        return collaboratorMapper.toResponse(collab);
    }

    @Transactional
    public ResetPasswordResponse resetPassword(UUID id, UUID requestingUserId) {
        Collaborator collab = findCollabOrThrow(id);
        if (collab.getPartner() != null) {
            permissionChecker.checkPartnerAccess(requestingUserId, collab.getPartner().getId());
        }

        String tempPassword = generateSecurePassword(12);
        collab.setPasswordHash(passwordEncoder.encode(tempPassword));
        collab.setRefreshTokenHash(null);
        collab.setRefreshTokenExpiry(null);
        collaboratorRepository.save(collab);

        auditService.log(requestingUserId, null, "PASSWORD_RESET",
                "Collaborator", collab.getId(), null, null,
                collab.getPartner() != null ? collab.getPartner().getCode() : null, null);

        return ResetPasswordResponse.builder()
                .message("Mot de passe réinitialisé avec succès")
                .temporaryPassword(tempPassword)
                .build();
    }

    @Transactional
    public void softDelete(UUID id, UUID requestingUserId) {
        Collaborator collab = findCollabOrThrow(id);
        if (collab.getPartner() != null) {
            permissionChecker.checkPartnerAccess(requestingUserId, collab.getPartner().getId());
        }

        collab.setDeleted(true);
        collaboratorRepository.save(collab);

        auditService.log(requestingUserId, null, "COLLABORATOR_DELETED",
                "Collaborator", collab.getId(), null, null,
                collab.getPartner() != null ? collab.getPartner().getCode() : null, null);
    }

    private Collaborator findCollabOrThrow(UUID id) {
        return collaboratorRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator", "id", id));
    }

    private String generateSecurePassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        // Ensure at least one of each required type
        sb.append(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        sb.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        sb.append(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));

        String allChars = UPPER + LOWER + DIGITS + SPECIAL;
        for (int i = 3; i < length; i++) {
            sb.append(allChars.charAt(RANDOM.nextInt(allChars.length())));
        }

        // Shuffle
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }
}
