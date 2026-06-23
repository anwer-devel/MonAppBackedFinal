package com.erp.platform.iam.service;

import com.erp.platform.core.audit.AuditService;
import com.erp.platform.core.common.PageResponse;
import com.erp.platform.core.exception.ConflictException;
import com.erp.platform.core.exception.ResourceNotFoundException;
import com.erp.platform.iam.dto.collaborator.CollaboratorResponse;
import com.erp.platform.iam.dto.partner.*;
import com.erp.platform.iam.entity.Collaborator;
import com.erp.platform.iam.entity.Partner;
import com.erp.platform.iam.entity.PartnerConfig;
import com.erp.platform.iam.enums.*;
import com.erp.platform.iam.mapper.CollaboratorMapper;
import com.erp.platform.iam.mapper.PartnerMapper;
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

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final CollaboratorRepository collaboratorRepository;
    private final LocalUnitRepository localUnitRepository;
    private final PartnerMapper partnerMapper;
    private final CollaboratorMapper collaboratorMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final TenantInitService tenantInitService;

    public PartnerStatsResponse getStats() {
        return PartnerStatsResponse.builder()
                .totalActive(partnerRepository.countActive())
                .totalTrial(partnerRepository.countTrial())
                .totalSuspended(partnerRepository.countSuspended())
                .totalMrr(BigDecimal.ZERO) // Will be computed from billing module
                .build();
    }

    public PageResponse<PartnerResponse> getAll(SectorType sector, PlanType plan,
                                                  PartnerStatus status, String q,
                                                  Pageable pageable) {
        Page<Partner> page = partnerRepository.findWithFilters(sector, plan, status, q, pageable);
        Page<PartnerResponse> responsePage = page.map(this::enrichPartnerResponse);
        return PageResponse.from(responsePage);
    }

    public PartnerResponse getById(UUID id) {
        Partner partner = findPartnerOrThrow(id);
        return enrichPartnerResponse(partner);
    }

    @Transactional
    public PartnerResponse create(CreatePartnerRequest req, UUID createdByUserId) {
        // Check uniqueness
        partnerRepository.findByCodeIgnoreCaseAndIsDeletedFalse(req.getCode())
                .ifPresent(p -> { throw new ConflictException("Ce code est déjà utilisé", "code"); });

        partnerRepository.findByEmailAndIsDeletedFalse(req.getEmail())
                .ifPresent(p -> { throw new ConflictException("Cet email est déjà utilisé", "email"); });

        // Create partner
        Partner partner = partnerMapper.toEntity(req);
        partner.setStatus(req.getPlan() == PlanType.TRIAL ? PartnerStatus.TRIAL : PartnerStatus.ACTIVE);
        partner.setConfig(buildDefaultConfig(req.getSectorType()));
        partner = partnerRepository.save(partner);

        // Initialize tenant schema
        tenantInitService.initializeTenant(partner);

        // Audit
        auditService.log(createdByUserId, null, "PARTNER_CREATED",
                "Partner", partner.getId(), null, partner, null, null);

        return enrichPartnerResponse(partner);
    }

    @Transactional
    public CollaboratorResponse createAdmin(UUID partnerId,
                                             CreatePartnerAdminRequest req,
                                             UUID createdByUserId) {
        Partner partner = findPartnerOrThrow(partnerId);

        collaboratorRepository.findByEmailAndIsDeletedFalse(req.getEmail())
                .ifPresent(c -> { throw new ConflictException("Cet email est déjà utilisé", "email"); });

        Collaborator admin = Collaborator.builder()
                .partner(partner)
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .role(CollaboratorRole.PARTNER_ADMIN)
                .status(CollaboratorStatus.ACTIVE)
                .build();

        admin = collaboratorRepository.save(admin);

        auditService.log(createdByUserId, null, "PARTNER_ADMIN_CREATED",
                "Collaborator", admin.getId(), null, null, partner.getCode(), null);

        return collaboratorMapper.toResponse(admin);
    }

    @Transactional
    public PartnerResponse update(UUID id, UpdatePartnerRequest req) {
        Partner partner = findPartnerOrThrow(id);
        String oldStatus = partner.getStatus().name();

        if (req.getName() != null) partner.setName(req.getName());
        if (req.getPhone() != null) partner.setPhone(req.getPhone());
        if (req.getAddress() != null) partner.setAddress(req.getAddress());
        if (req.getTaxNumber() != null) partner.setTaxNumber(req.getTaxNumber());
        if (req.getPlan() != null) partner.setPlan(req.getPlan());
        if (req.getSubscriptionEnd() != null) partner.setSubscriptionEnd(req.getSubscriptionEnd());

        partner = partnerRepository.save(partner);
        return enrichPartnerResponse(partner);
    }

    @Transactional
    public PartnerResponse updateStatus(UUID id, PartnerStatus status) {
        Partner partner = findPartnerOrThrow(id);
        String oldStatus = partner.getStatus().name();

        partner.setStatus(status);
        partner = partnerRepository.save(partner);

        auditService.log(null, null, "PARTNER_STATUS_CHANGED",
                "Partner", partner.getId(), oldStatus, status.name(),
                partner.getCode(), null);

        return enrichPartnerResponse(partner);
    }

    public PartnerConfig getConfig(UUID id) {
        Partner partner = findPartnerOrThrow(id);
        return partner.getConfig();
    }

    @Transactional
    public PartnerResponse updateConfig(UUID id, PartnerConfigRequest req) {
        Partner partner = findPartnerOrThrow(id);

        PartnerConfig config = PartnerConfig.builder()
                .stockEnabled(req.isStockEnabled())
                .posEnabled(req.isPosEnabled())
                .billingEnabled(req.isBillingEnabled())
                .purchaseEnabled(req.isPurchaseEnabled())
                .reportsEnabled(req.isReportsEnabled())
                .multiWarehouseEnabled(req.isMultiWarehouseEnabled())
                .vehicleCompatEnabled(req.isVehicleCompatEnabled())
                .vinLookupEnabled(req.isVinLookupEnabled())
                .expiryTrackingEnabled(req.isExpiryTrackingEnabled())
                .lotTrackingEnabled(req.isLotTrackingEnabled())
                .prescriptionEnabled(req.isPrescriptionEnabled())
                .defaultCurrency(req.getDefaultCurrency())
                .defaultTaxRate(req.getDefaultTaxRate())
                .invoicePrefix(req.getInvoicePrefix())
                .invoiceStartNumber(req.getInvoiceStartNumber())
                .stockValuationMethod(req.getStockValuationMethod())
                .build();

        partner.setConfig(config);
        partner = partnerRepository.save(partner);

        auditService.log(null, null, "PARTNER_CONFIG_UPDATED",
                "Partner", partner.getId(), null, config, partner.getCode(), null);

        return enrichPartnerResponse(partner);
    }

    public SetupStatusResponse getSetupStatus(UUID partnerId) {
        int localsCount = localUnitRepository.countByPartner_IdAndIsDeletedFalse(partnerId);
        int collabsCount = collaboratorRepository.countByPartner_IdAndIsDeletedFalse(partnerId);
        // Subtract the PARTNER_ADMIN from collaborators count
        return SetupStatusResponse.builder()
                .hasLocals(localsCount > 0)
                .hasCollaborators(collabsCount > 1) // > 1 because PARTNER_ADMIN always exists
                .build();
    }

    @Transactional
    public void softDelete(UUID id) {
        Partner partner = findPartnerOrThrow(id);
        partner.setDeleted(true);
        partnerRepository.save(partner);

        auditService.log(null, null, "PARTNER_DELETED",
                "Partner", partner.getId(), null, null, partner.getCode(), null);
    }

    private Partner findPartnerOrThrow(UUID id) {
        return partnerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner", "id", id));
    }

    private PartnerResponse enrichPartnerResponse(Partner partner) {
        PartnerResponse response = partnerMapper.toResponse(partner);
        response.setLocalsCount(localUnitRepository.countByPartner_IdAndIsDeletedFalse(partner.getId()));
        response.setCollaboratorsCount(
                collaboratorRepository.countByPartner_IdAndIsDeletedFalse(partner.getId()));
        response.setMonthlyRevenue(BigDecimal.ZERO); // Will come from billing module
        return response;
    }

    private PartnerConfig buildDefaultConfig(SectorType sectorType) {
        PartnerConfig.PartnerConfigBuilder builder = PartnerConfig.builder()
                .stockEnabled(true)
                .posEnabled(true)
                .billingEnabled(true)
                .defaultCurrency("TND")
                .defaultTaxRate(new BigDecimal("19.0"))
                .invoicePrefix("FAC")
                .invoiceStartNumber(1000)
                .stockValuationMethod("AVERAGE_COST");

        switch (sectorType) {
            case AUTO -> builder
                    .vehicleCompatEnabled(true)
                    .vinLookupEnabled(true);
            case PHARMA -> builder
                    .expiryTrackingEnabled(true)
                    .lotTrackingEnabled(true)
                    .prescriptionEnabled(true)
                    .stockValuationMethod("FEFO");
            case HARDWARE -> builder
                    .multiWarehouseEnabled(false);
            default -> { /* MIXED: defaults only */ }
        }

        return builder.build();
    }
}
