package com.app.zone.service.impl;

import com.app.common.exception.BadRequestException;
import com.app.common.exception.ResourceNotFoundException;
import com.app.zone.dto.CreateZoneRequest;
import com.app.zone.dto.ZoneDto;
import com.app.zone.entity.Zone;
import com.app.zone.repository.ZoneRepository;
import com.app.zone.service.ZoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZoneServiceImpl implements ZoneService {

    private final ZoneRepository zoneRepository;

    @Override
    @Transactional
    public ZoneDto createZone(CreateZoneRequest request) {
        if (zoneRepository.findByNameAndIsActiveTrue(request.getName()).isPresent()) {
            throw new BadRequestException("Zone with this name already exists");
        }

        Zone zone = Zone.builder()
                .name(request.getName())
                .description(request.getDescription())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .partnerCount(0)
                .build();

        zone = zoneRepository.save(zone);
        log.info("Zone created: {}", zone.getId());
        return mapToDto(zone);
    }

    @Override
    @Transactional(readOnly = true)
    public ZoneDto getZoneById(UUID id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + id));
        return mapToDto(zone);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZoneDto> getAllZones() {
        return zoneRepository.findByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional
    public ZoneDto updateZone(UUID id, CreateZoneRequest request) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + id));

        zone.setName(request.getName());
        zone.setDescription(request.getDescription());
        zone.setLatitude(request.getLatitude());
        zone.setLongitude(request.getLongitude());

        zone = zoneRepository.save(zone);
        log.info("Zone updated: {}", zone.getId());
        return mapToDto(zone);
    }

    @Override
    @Transactional
    public void deleteZone(UUID id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + id));

        zone.setIsActive(false);
        zoneRepository.save(zone);
        log.info("Zone deleted: {}", id);
    }

    private ZoneDto mapToDto(Zone zone) {
        return ZoneDto.builder()
                .id(zone.getId())
                .name(zone.getName())
                .description(zone.getDescription())
                .latitude(zone.getLatitude())
                .longitude(zone.getLongitude())
                .partnerCount(zone.getPartnerCount())
                .build();
    }
}

