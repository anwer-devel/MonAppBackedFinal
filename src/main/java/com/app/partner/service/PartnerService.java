package com.app.partner.service;

import com.app.partner.dto.CreatePartnerRequest;
import com.app.partner.dto.LocationDto;
import com.app.partner.dto.PartnerAssetDto;
import com.app.partner.dto.PartnerDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface PartnerService {
    PartnerDto createPartner(CreatePartnerRequest request, UUID ownerId);
    PartnerDto getPartnerById(UUID id);
    Page<PartnerDto> getPartnersByZone(UUID zoneId, Pageable pageable);
    Page<PartnerDto> getTopPartnersByZone(UUID zoneId, Pageable pageable);
    List<PartnerDto> getMyPartners(UUID ownerId);
    PartnerDto updatePartner(UUID id, CreatePartnerRequest request, UUID ownerId);
    void deletePartner(UUID id, UUID ownerId);
    void verifyPartner(UUID id);

    // Location
    LocationDto setLocation(UUID partnerId, LocationDto locationDto, UUID ownerId);
    LocationDto getLocation(UUID partnerId);

    // Assets
    PartnerAssetDto uploadAsset(UUID partnerId, MultipartFile file, String type, UUID ownerId);
    List<PartnerAssetDto> getAssets(UUID partnerId);
    void deleteAsset(UUID assetId, UUID ownerId);
}

