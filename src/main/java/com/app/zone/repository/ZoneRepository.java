package com.app.zone.repository;

import com.app.zone.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, UUID> {
    Optional<Zone> findByNameAndIsActiveTrue(String name);
    List<Zone> findByIsActiveTrueOrderByNameAsc();
}

