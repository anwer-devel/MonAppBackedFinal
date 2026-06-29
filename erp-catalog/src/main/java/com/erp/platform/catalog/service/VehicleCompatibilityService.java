package com.erp.platform.catalog.service;

import com.erp.platform.catalog.dto.request.VehicleSearchRequest;
import com.erp.platform.catalog.dto.response.ProductSummaryResponse;
import com.erp.platform.catalog.mapper.ProductMapper;
import com.erp.platform.catalog.repository.VehicleCompatibilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleCompatibilityService {

    private final VehicleCompatibilityRepository compatibilityRepository;
    private final ProductMapper productMapper;

    public List<String> getDistinctMakes() {
        return compatibilityRepository.findDistinctMakes();
    }

    public List<String> getModelsByMake(String make) {
        return compatibilityRepository.findModelsByMake(make);
    }

    public List<ProductSummaryResponse> findCompatibleProducts(VehicleSearchRequest req) {
        return compatibilityRepository.findCompatibleProducts(req.getMake(), req.getModel(), req.getYear())
                .stream()
                .map(productMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }
}
