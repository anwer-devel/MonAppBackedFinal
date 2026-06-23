package com.erp.platform.iam.repository;

import com.erp.platform.iam.entity.Partner;
import com.erp.platform.iam.enums.PartnerStatus;
import com.erp.platform.iam.enums.PlanType;
import com.erp.platform.iam.enums.SectorType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, UUID> {

    Optional<Partner> findByCodeIgnoreCaseAndIsDeletedFalse(String code);

    Optional<Partner> findByEmailAndIsDeletedFalse(String email);

    Optional<Partner> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT p FROM Partner p WHERE p.isDeleted = false " +
            "AND (:sector IS NULL OR p.sectorType = :sector) " +
            "AND (:plan IS NULL OR p.plan = :plan) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "  OR LOWER(p.code) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "  OR LOWER(p.email) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Partner> findWithFilters(@Param("sector") SectorType sector,
                                  @Param("plan") PlanType plan,
                                  @Param("status") PartnerStatus status,
                                  @Param("q") String q,
                                  Pageable pageable);

    @Query("SELECT COUNT(p) FROM Partner p WHERE p.status = 'ACTIVE' AND p.isDeleted = false")
    long countActive();

    @Query("SELECT COUNT(p) FROM Partner p WHERE p.status = 'TRIAL' AND p.isDeleted = false")
    long countTrial();

    @Query("SELECT COUNT(p) FROM Partner p WHERE p.status = 'SUSPENDED' AND p.isDeleted = false")
    long countSuspended();
}
