package com.erp.platform.catalog.repository;

import com.erp.platform.catalog.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UnitRepository extends JpaRepository<Unit, UUID> {
    List<Unit> findByIsDeletedFalseOrderByNameAsc();
    Optional<Unit> findByIsDefaultTrueAndIsDeletedFalse();
    Optional<Unit> findByCodeIgnoreCaseAndIsDeletedFalse(String code);
}
