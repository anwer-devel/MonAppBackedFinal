package com.app.partner.repository;

import com.app.partner.entity.Partner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, UUID> {
    Page<Partner> findByZoneIdAndIsActiveTrue(UUID zoneId, Pageable pageable);

    Optional<Partner> findByIdAndIsActiveTrue(UUID id);

    List<Partner> findByOwnerIdAndIsActiveTrue(UUID ownerId);

    @Query("SELECT p FROM Partner p WHERE p.zone.id = :zoneId AND p.isActive = true ORDER BY p.rating DESC")
    Page<Partner> findTopPartnersByZone(@Param("zoneId") UUID zoneId, Pageable pageable);

    Optional<Partner> findByNameAndZoneIdAndIsActiveTrueAndIdNot(String name, UUID zoneId, UUID id);
}

