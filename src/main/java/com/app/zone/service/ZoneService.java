package com.app.zone.service;

import com.app.zone.dto.CreateZoneRequest;
import com.app.zone.dto.ZoneDto;

import java.util.List;
import java.util.UUID;

public interface ZoneService {
    ZoneDto createZone(CreateZoneRequest request);
    ZoneDto getZoneById(UUID id);
    List<ZoneDto> getAllZones();
    ZoneDto updateZone(UUID id, CreateZoneRequest request);
    void deleteZone(UUID id);
}

