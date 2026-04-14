package com.app.partner.repository;

import com.app.partner.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {
    Optional<Location> findByPartnerIdAndIsActiveTrue(UUID partnerId);
}

