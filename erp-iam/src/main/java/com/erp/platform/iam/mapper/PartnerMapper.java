package com.erp.platform.iam.mapper;

import com.erp.platform.iam.dto.partner.CreatePartnerRequest;
import com.erp.platform.iam.dto.partner.PartnerResponse;
import com.erp.platform.iam.entity.Partner;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface PartnerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "config", ignore = true)
    @Mapping(target = "currency", constant = "TND")
    Partner toEntity(CreatePartnerRequest request);

    @Mapping(target = "localsCount", ignore = true)
    @Mapping(target = "collaboratorsCount", ignore = true)
    @Mapping(target = "monthlyRevenue", ignore = true)
    PartnerResponse toResponse(Partner partner);
}
