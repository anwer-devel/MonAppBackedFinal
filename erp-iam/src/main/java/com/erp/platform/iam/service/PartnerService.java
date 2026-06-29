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
@Transactional(readOnly = true)
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
        String qParam = (q != null && !q.isBlank()) ? q.trim() : null;
        String qPattern = (qParam != null) ? "%" + qParam.toLowerCase() + "%" : null;
        Page<Partner> page = partnerRepository.findWithFilters(sector, plan, status, qParam, qPattern, pageable);
        Page<PartnerResponse> responsePage = page.map(this::enrichPartnerResponse);
        return PageResponse.from(responsePage);
    }

    public PartnerResponse getById(UUID id) {
        Partner partner = findPartnerOrThrow(id);
        return enrichPartnerResponse(partner);
    }

    @Transactional
    public PartnerResponse create(CreatePartnerRequest req, UUID createdByUserId) {
        // Normaliser le code
        if (req.getCode() != null) {
            req.setCode(req.getCode().trim().toUpperCase());
        }

        // Vérifier unicité code
        if (partnerRepository.findByCodeIgnoreCaseAndIsDeletedFalse(req.getCode()).isPresent()) {
            throw new ConflictException("Ce code partenaire est déjà utilisé", "code");
        }

        // Vérifier unicité email
        if (partnerRepository.findByEmailIgnoreCaseAndIsDeletedFalse(req.getEmail()).isPresent()) {
            throw new ConflictException("Cet email est déjà utilisé", "email");
        }

        PartnerConfig config = buildDefaultConfig(req.getSectorType());

        Partner partner = Partner.builder()
                .code(req.getCode())
                .name(req.getName())
                .sectorType(req.getSectorType())
                .plan(req.getPlan())
                .status(req.getPlan() == PlanType.TRIAL ? PartnerStatus.TRIAL : PartnerStatus.ACTIVE)
                .email(req.getEmail())
                .phone(req.getPhone())
                .address(req.getAddress())
                .taxNumber(req.getTaxNumber())
                .subscriptionEnd(req.getSubscriptionEnd())
                .currency("TND")
                .config(config)
                .build();

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
        partnerRepository.findByIdAndIsDeletedFalse(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner", "id", partnerId));

        int localsCount = localUnitRepository.countByPartner_IdAndIsDeletedFalse(partnerId);
        int collabsCount = collaboratorRepository.countNonAdminByPartnerId(partnerId);

        return SetupStatusResponse.builder()
                .hasLocals(localsCount > 0)
                .hasCollaborators(collabsCount > 0)
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

    private PartnerConfig buildDefaultConfig(SectorType sector) {
        PartnerConfig cfg = new PartnerConfig();
        cfg.setStockEnabled(true);
        cfg.setPosEnabled(true);
        cfg.setBillingEnabled(true);
        cfg.setReportsEnabled(true);
        cfg.setDefaultCurrency("TND");
        cfg.setDefaultTaxRate(BigDecimal.valueOf(19.0));
        cfg.setInvoicePrefix("FAC");
        cfg.setInvoiceStartNumber(1000);
        cfg.setStockValuationMethod("AVERAGE_COST");
        if (sector == SectorType.AUTO || sector == SectorType.MIXED) {
            cfg.setVehicleCompatEnabled(true);
            cfg.setVinLookupEnabled(true);
        }
        if (sector == SectorType.PHARMA) {
            cfg.setExpiryTrackingEnabled(true);
            cfg.setLotTrackingEnabled(true);
            cfg.setPrescriptionEnabled(true);
            cfg.setStockValuationMethod("FEFO");
        }
        return cfg;
    }
}
