package com.erp.platform.inventory.repository;

import com.erp.platform.inventory.entity.PhysicalInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PhysicalInventoryRepository extends JpaRepository<PhysicalInventory, UUID> {

    @Query("SELECT DISTINCT i FROM PhysicalInventory i LEFT JOIN FETCH i.lines WHERE i.localId = :localId AND i.isDeleted = false ORDER BY i.startedAt DESC")
    List<PhysicalInventory> findByLocalIdAndIsDeletedFalseOrderByStartedAtDesc(@Param("localId") UUID localId);

    @Query("SELECT DISTINCT i FROM PhysicalInventory i LEFT JOIN FETCH i.lines WHERE i.isDeleted = false ORDER BY i.startedAt DESC")
    List<PhysicalInventory> findByIsDeletedFalseOrderByStartedAtDesc();

    @Query("SELECT DISTINCT i FROM PhysicalInventory i LEFT JOIN FETCH i.lines WHERE i.id = :id AND i.isDeleted = false")
    Optional<PhysicalInventory> findByIdAndIsDeletedFalse(@Param("id") UUID id);
}
