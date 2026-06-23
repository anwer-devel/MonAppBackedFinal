package com.erp.platform.iam.mapper;

import com.erp.platform.iam.dto.collaborator.CollaboratorResponse;
import com.erp.platform.iam.entity.Collaborator;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CollaboratorMapper {

    @Mapping(source = "partner.id", target = "partnerId")
    @Mapping(source = "partner.name", target = "partnerName")
    @Mapping(source = "defaultLocal.id", target = "defaultLocalId")
    @Mapping(source = "defaultLocal.name", target = "defaultLocalName")
    CollaboratorResponse toResponse(Collaborator collaborator);
}
