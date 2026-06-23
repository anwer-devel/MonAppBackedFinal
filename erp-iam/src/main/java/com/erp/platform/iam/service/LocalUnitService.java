package com.erp.platform.iam.service;

import com.erp.platform.core.audit.AuditService;
import com.erp.platform.core.exception.ConflictException;
import com.erp.platform.core.exception.ResourceNotFoundException;
import com.erp.platform.iam.dto.local.*;
import com.erp.platform.iam.entity.LocalUnit;
import com.erp.platform.iam.entity.Partner;
import com.erp.platform.iam.enums.LocalStatus;
import com.erp.platform.iam.enums.LocalType;
import com.erp.platform.iam.mapper.LocalUnitMapper;
import com.erp.platform.iam.repository.CollaboratorRepository;
import com.erp.platform.iam.repository.LocalUnitRepository;
import com.erp.platform.iam.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalUnitService {

    private final LocalUnitRepository localUnitRepository;
    private final PartnerRepository partnerRepository;
    private final CollaboratorRepository collaboratorRepository;
    private final LocalUnitMapper localUnitMapper;
    private final PermissionChecker permissionChecker;
    private final AuditService auditService;

    public List<LocalResponse> getByPartner(UUID partnerId, LocalType type,
                                             UUID requestingUserId) {
        permissionChecker.checkPartnerAccess(requestingUserId, partnerId);

        List<LocalUnit> locals;
        if (type != null) {
            locals = localUnitRepository.findByPartner_IdAndTypeAndIsDeletedFalse(partnerId, type);
        } else {
            locals = localUnitRepository.findByPartner_IdAndIsDeletedFalse(partnerId);
        }

        return locals.stream()
                .map(this::enrichLocalResponse)
                .collect(Collectors.toList());
    }

    public LocalResponse getById(UUID id) {
        LocalUnit local = findLocalOrThrow(id);
        return enrichLocalResponse(local);
    }

    @Transactional
    public LocalResponse create(UUID partnerId, CreateLocalRequest req, UUID createdByUserId) {
        permissionChecker.checkPartnerAccess(createdByUserId, partnerId);

        Partner partner = partnerRepository.findByIdAndIsDeletedFalse(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner", "id", partnerId));

        // Check code uniqueness within partner
        if (localUnitRepository.existsByPartner_IdAndCodeAndIsDeletedFalse(partnerId, req.getCode())) {
            throw new ConflictException("Ce code de local existe déjà pour ce partenaire", "code");
        }

        // Check MAIN uniqueness
        if (req.isMain()) {
            localUnitRepository.findByPartner_IdAndIsMainTrueAndIsDeletedFalse(partnerId)
                    .ifPresent(existing -> {
                        throw new ConflictException(
                                "Un local principal existe déjà pour ce partenaire", "isMain");
                    });
        }

        LocalUnit local = localUnitMapper.toEntity(req);
        local.setPartner(partner);
        local.setStatus(LocalStatus.ACTIVE);
        local = localUnitRepository.save(local);

        auditService.log(createdByUserId, null, "LOCAL_CREATED",
                "LocalUnit", local.getId(), null, null, partner.getCode(), null);

        return enrichLocalResponse(local);
    }

    @Transactional
    public LocalResponse update(UUID id, UpdateLocalRequest req, UUID requestingUserId) {
        LocalUnit local = findLocalOrThrow(id);
        permissionChecker.checkPartnerAccess(requestingUserId, local.getPartner().getId());

        if (req.getName() != null) local.setName(req.getName());
        if (req.getAddress() != null) local.setAddress(req.getAddress());
        if (req.getPhone() != null) local.setPhone(req.getPhone());
        if (req.getEmail() != null) local.setEmail(req.getEmail());

        local = localUnitRepository.save(local);
        return enrichLocalResponse(local);
    }

    @Transactional
    public LocalResponse updateStatus(UUID id, LocalStatus status, UUID requestingUserId) {
        LocalUnit local = findLocalOrThrow(id);
        permissionChecker.checkPartnerAccess(requestingUserId, local.getPartner().getId());

        local.setStatus(status);
        local = localUnitRepository.save(local);
        return enrichLocalResponse(local);
    }

    @Transactional
    public void softDelete(UUID id, UUID requestingUserId) {
        LocalUnit local = findLocalOrThrow(id);
        permissionChecker.checkPartnerAccess(requestingUserId, local.getPartner().getId());

        local.setDeleted(true);
        localUnitRepository.save(local);

        auditService.log(requestingUserId, null, "LOCAL_DELETED",
                "LocalUnit", local.getId(), null, null,
                local.getPartner().getCode(), null);
    }

    private LocalUnit findLocalOrThrow(UUID id) {
        return localUnitRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("LocalUnit", "id", id));
    }

    private LocalResponse enrichLocalResponse(LocalUnit local) {
        LocalResponse response = localUnitMapper.toResponse(local);
        // Count collaborators assigned to this local
        response.setCollaboratorsCount(0); // simplified — would query by defaultLocal or localAccess
        return response;
    }
}
