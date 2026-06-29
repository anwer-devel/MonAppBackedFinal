package com.erp.platform.iam.mapper;

import com.erp.platform.iam.dto.local.CreateLocalRequest;
import com.erp.platform.iam.dto.local.LocalResponse;
import com.erp.platform.iam.entity.LocalUnit;
import com.erp.platform.iam.entity.Partner;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-29T15:49:26+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Amazon.com Inc.)"
)
@Component
public class LocalUnitMapperImpl implements LocalUnitMapper {

    @Override
    public LocalUnit toEntity(CreateLocalRequest request) {
        if ( request == null ) {
            return null;
        }

        LocalUnit localUnit = new LocalUnit();

        localUnit.setCode( request.getCode() );
        localUnit.setName( request.getName() );
        localUnit.setType( request.getType() );
        localUnit.setAddress( request.getAddress() );
        localUnit.setPhone( request.getPhone() );
        localUnit.setEmail( request.getEmail() );
        localUnit.setMain( request.isMain() );

        return localUnit;
    }

    @Override
    public LocalResponse toResponse(LocalUnit localUnit) {
        if ( localUnit == null ) {
            return null;
        }

        LocalResponse localResponse = new LocalResponse();

        localResponse.setPartnerId( localUnitPartnerId( localUnit ) );
        localResponse.setMain( localUnit.isMain() );
        localResponse.setId( localUnit.getId() );
        localResponse.setCode( localUnit.getCode() );
        localResponse.setName( localUnit.getName() );
        localResponse.setType( localUnit.getType() );
        localResponse.setAddress( localUnit.getAddress() );
        localResponse.setPhone( localUnit.getPhone() );
        localResponse.setEmail( localUnit.getEmail() );
        localResponse.setStatus( localUnit.getStatus() );
        localResponse.setCreatedAt( localUnit.getCreatedAt() );

        return localResponse;
    }

    private UUID localUnitPartnerId(LocalUnit localUnit) {
        if ( localUnit == null ) {
            return null;
        }
        Partner partner = localUnit.getPartner();
        if ( partner == null ) {
            return null;
        }
        UUID id = partner.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
