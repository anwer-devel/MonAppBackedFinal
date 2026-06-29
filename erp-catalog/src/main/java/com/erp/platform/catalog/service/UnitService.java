package com.erp.platform.catalog.service;

import com.erp.platform.catalog.dto.request.CreateUnitRequest;
import com.erp.platform.catalog.dto.response.UnitResponse;
import com.erp.platform.catalog.entity.Unit;
import com.erp.platform.catalog.mapper.UnitMapper;
import com.erp.platform.catalog.repository.UnitRepository;
import com.erp.platform.core.exception.ConflictException;
import com.erp.platform.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnitService {

    private final UnitRepository unitRepository;
    private final UnitMapper unitMapper;

    public List<UnitResponse> getAll() {
        return unitMapper.toResponseList(unitRepository.findByIsDeletedFalseOrderByNameAsc());
    }

    @Transactional
    public UnitResponse create(CreateUnitRequest req) {
        if (unitRepository.findByCodeIgnoreCaseAndIsDeletedFalse(req.getCode()).isPresent()) {
            throw new ConflictException("Le code unité '" + req.getCode() + "' est déjà utilisé", "code");
        }
        Unit unit = unitMapper.toEntity(req);
        return unitMapper.toResponse(unitRepository.save(unit));
    }

    @Transactional
    public UnitResponse update(UUID id, CreateUnitRequest req) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", id));
        if (!unit.getCode().equalsIgnoreCase(req.getCode())) {
            if (unitRepository.findByCodeIgnoreCaseAndIsDeletedFalse(req.getCode()).isPresent()) {
                throw new ConflictException("Le code unité '" + req.getCode() + "' est déjà utilisé", "code");
            }
        }
        unitMapper.updateEntityFromRequest(req, unit);
        return unitMapper.toResponse(unitRepository.save(unit));
    }
}
