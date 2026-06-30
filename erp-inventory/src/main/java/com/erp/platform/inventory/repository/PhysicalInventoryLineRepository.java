package com.erp.platform.inventory.repository;

import com.erp.platform.inventory.entity.PhysicalInventoryLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PhysicalInventoryLineRepository extends JpaRepository<PhysicalInventoryLine, UUID> {
}
