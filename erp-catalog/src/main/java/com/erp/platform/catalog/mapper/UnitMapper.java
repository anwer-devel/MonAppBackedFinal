package com.erp.platform.catalog.mapper;

import com.erp.platform.catalog.dto.request.CreateUnitRequest;
import com.erp.platform.catalog.dto.response.UnitResponse;
import com.erp.platform.catalog.entity.Unit;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface UnitMapper {

    UnitResponse toResponse(Unit unit);

    List<UnitResponse> toResponseList(List<Unit> units);

    @Mapping(target = "default", constant = "false")
    Unit toEntity(CreateUnitRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(CreateUnitRequest request, @MappingTarget Unit unit);
}
