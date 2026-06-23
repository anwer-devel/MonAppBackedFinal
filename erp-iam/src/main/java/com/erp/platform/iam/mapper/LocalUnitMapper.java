package com.erp.platform.iam.mapper;

import com.erp.platform.iam.dto.local.CreateLocalRequest;
import com.erp.platform.iam.dto.local.LocalResponse;
import com.erp.platform.iam.entity.LocalUnit;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LocalUnitMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "partner", ignore = true)
    @Mapping(target = "status", ignore = true)
    LocalUnit toEntity(CreateLocalRequest request);

    @Mapping(source = "partner.id", target = "partnerId")
    @Mapping(target = "collaboratorsCount", ignore = true)
    @Mapping(source = "main", target = "isMain")
    LocalResponse toResponse(LocalUnit localUnit);
}
