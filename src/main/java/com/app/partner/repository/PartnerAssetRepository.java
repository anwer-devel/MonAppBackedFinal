package com.app.partner.repository;

import com.app.partner.entity.PartnerAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PartnerAssetRepository extends JpaRepository<PartnerAsset, UUID> {
    List<PartnerAsset> findByPartnerIdAndIsActiveTrueOrderByDisplayOrderAsc(UUID partnerId);

    List<PartnerAsset> findByPartnerIdAndIsActiveTrue(UUID partnerId);
}

