package com.app.partner.service.impl;

import com.app.auth.repository.UserRepository;
import com.app.common.exception.BadRequestException;
import com.app.common.exception.ForbiddenException;
import com.app.common.exception.ResourceNotFoundException;
import com.app.partner.dto.CreatePartnerRequest;
import com.app.partner.dto.LocationDto;
import com.app.partner.dto.PartnerAssetDto;
import com.app.partner.dto.PartnerDto;
import com.app.partner.entity.Location;
import com.app.partner.entity.Partner;
import com.app.partner.entity.PartnerAsset;
import com.app.partner.repository.LocationRepository;
import com.app.partner.repository.PartnerAssetRepository;
import com.app.partner.repository.PartnerRepository;
import com.app.partner.service.PartnerService;
import com.app.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepository partnerRepository;
    private final LocationRepository locationRepository;
    private final PartnerAssetRepository partnerAssetRepository;
    private final ZoneRepository zoneRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.upload.path}")
    private String uploadPath;

    @Override
    @Transactional
    public PartnerDto createPartner(CreatePartnerRequest request, UUID ownerId) {
        var zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found"));

        var owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Partner partner = Partner.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(Partner.PartnerType.valueOf(request.getType()))
                .zone(zone)
                .owner(owner)
                .isVerified(false)
                .rating(0.0)
                .assetCount(0)
                .build();

        partner = partnerRepository.save(partner);
        zone.setPartnerCount(zone.getPartnerCount() + 1);
        zoneRepository.save(zone);

        invalidateZoneCache(request.getZoneId());
        log.info("Partner created: {}", partner.getId());

        return mapToDto(partner);
    }

    @Override
    @Transactional(readOnly = true)
    public PartnerDto getPartnerById(UUID id) {
        Partner partner = partnerRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));
        return mapToDto(partner);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PartnerDto> getPartnersByZone(UUID zoneId, Pageable pageable) {
        String cacheKey = "zone:" + zoneId + ":partners";

        // Try to get from cache (simplified - in production use proper pagination caching)
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("Cache hit for: {}", cacheKey);
        }

        Page<Partner> partners = partnerRepository.findByZoneIdAndIsActiveTrue(zoneId, pageable);
        return partners.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PartnerDto> getTopPartnersByZone(UUID zoneId, Pageable pageable) {
        Page<Partner> partners = partnerRepository.findTopPartnersByZone(zoneId, pageable);
        return partners.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartnerDto> getMyPartners(UUID ownerId) {
        List<Partner> partners = partnerRepository.findByOwnerIdAndIsActiveTrue(ownerId);
        return partners.stream().map(this::mapToDto).toList();
    }

    @Override
    @Transactional
    public PartnerDto updatePartner(UUID id, CreatePartnerRequest request, UUID ownerId) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        if (!partner.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("You can only update your own partners");
        }

        partner.setName(request.getName());
        partner.setDescription(request.getDescription());
        partner.setType(Partner.PartnerType.valueOf(request.getType()));

        partner = partnerRepository.save(partner);
        invalidateZoneCache(partner.getZone().getId());
        log.info("Partner updated: {}", partner.getId());

        return mapToDto(partner);
    }

    @Override
    @Transactional
    public void deletePartner(UUID id, UUID ownerId) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        if (!partner.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("You can only delete your own partners");
        }

        partner.setIsActive(false);
        partnerRepository.save(partner);
        partner.getZone().setPartnerCount(Math.max(0, partner.getZone().getPartnerCount() - 1));
        zoneRepository.save(partner.getZone());

        invalidateZoneCache(partner.getZone().getId());
        log.info("Partner deleted: {}", id);
    }

    @Override
    @Transactional
    public void verifyPartner(UUID id) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        partner.setIsVerified(true);
        partnerRepository.save(partner);
        invalidateZoneCache(partner.getZone().getId());
        log.info("Partner verified: {}", id);
    }

    @Override
    @Transactional
    public LocationDto setLocation(UUID partnerId, LocationDto locationDto, UUID ownerId) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        if (!partner.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("You can only set location for your own partners");
        }

        Location location = locationRepository.findByPartnerIdAndIsActiveTrue(partnerId)
                .orElse(Location.builder().partner(partner).build());

        location.setLatitude(locationDto.getLatitude());
        location.setLongitude(locationDto.getLongitude());
        location.setAddress(locationDto.getAddress());
        location.setCity(locationDto.getCity());
        location.setCountry(locationDto.getCountry());

        location = locationRepository.save(location);
        log.info("Location set for partner: {}", partnerId);

        return mapLocationToDto(location);
    }

    @Override
    @Transactional(readOnly = true)
    public LocationDto getLocation(UUID partnerId) {
        Location location = locationRepository.findByPartnerIdAndIsActiveTrue(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
        return mapLocationToDto(location);
    }

    @Override
    @Transactional
    public PartnerAssetDto uploadAsset(UUID partnerId, MultipartFile file, String type, UUID ownerId) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        if (!partner.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("You can only upload assets for your own partners");
        }

        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadDir = Paths.get(uploadPath);
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(fileName);
            Files.write(filePath, file.getBytes());

            PartnerAsset asset = PartnerAsset.builder()
                    .partner(partner)
                    .type(PartnerAsset.AssetType.valueOf(type))
                    .url("/uploads/" + fileName)
                    .displayOrder(partner.getAssetCount())
                    .build();

            asset = partnerAssetRepository.save(asset);
            partner.setAssetCount(partner.getAssetCount() + 1);
            partnerRepository.save(partner);

            invalidateZoneCache(partner.getZone().getId());
            log.info("Asset uploaded for partner: {}", partnerId);

            return mapAssetToDto(asset);
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartnerAssetDto> getAssets(UUID partnerId) {
        return partnerAssetRepository.findByPartnerIdAndIsActiveTrueOrderByDisplayOrderAsc(partnerId)
                .stream()
                .map(this::mapAssetToDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteAsset(UUID assetId, UUID ownerId) {
        PartnerAsset asset = partnerAssetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

        if (!asset.getPartner().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("You can only delete your own assets");
        }

        asset.setIsActive(false);
        partnerAssetRepository.save(asset);
        asset.getPartner().setAssetCount(Math.max(0, asset.getPartner().getAssetCount() - 1));
        partnerRepository.save(asset.getPartner());

        invalidateZoneCache(asset.getPartner().getZone().getId());
        log.info("Asset deleted: {}", assetId);
    }

    private PartnerDto mapToDto(Partner partner) {
        return PartnerDto.builder()
                .id(partner.getId())
                .name(partner.getName())
                .description(partner.getDescription())
                .type(partner.getType())
                .rating(partner.getRating())
                .isVerified(partner.getIsVerified())
                .zoneId(partner.getZone().getId())
                .ownerId(partner.getOwner().getId())
                .assetCount(partner.getAssetCount())
                .build();
    }

    private LocationDto mapLocationToDto(Location location) {
        return LocationDto.builder()
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .address(location.getAddress())
                .city(location.getCity())
                .country(location.getCountry())
                .build();
    }

    private PartnerAssetDto mapAssetToDto(PartnerAsset asset) {
        return PartnerAssetDto.builder()
                .id(asset.getId())
                .type(asset.getType())
                .url(asset.getUrl())
                .metadataJson(asset.getMetadataJson())
                .displayOrder(asset.getDisplayOrder())
                .build();
    }

    private void invalidateZoneCache(UUID zoneId) {
        String cacheKey = "zone:" + zoneId + ":partners";
        redisTemplate.delete(cacheKey);
        log.debug("Invalidated cache: {}", cacheKey);
    }
}

