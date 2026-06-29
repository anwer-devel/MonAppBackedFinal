package com.erp.platform.iam.mapper;

import com.erp.platform.iam.dto.partner.CreatePartnerRequest;
import com.erp.platform.iam.dto.partner.PartnerResponse;
import com.erp.platform.iam.entity.Partner;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-29T09:53:18+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Amazon.com Inc.)"
)
@Component
public class PartnerMapperImpl implements PartnerMapper {

    @Override
    public Partner toEntity(CreatePartnerRequest request) {
        if ( request == null ) {
            return null;
        }

        Partner partner = new Partner();

        partner.setCode( request.getCode() );
        partner.setName( request.getName() );
        partner.setSectorType( request.getSectorType() );
        partner.setPlan( request.getPlan() );
        partner.setEmail( request.getEmail() );
        partner.setPhone( request.getPhone() );
        partner.setAddress( request.getAddress() );
        partner.setTaxNumber( request.getTaxNumber() );
        partner.setSubscriptionEnd( request.getSubscriptionEnd() );

        partner.setCurrency( "TND" );

        return partner;
    }

    @Override
    public PartnerResponse toResponse(Partner partner) {
        if ( partner == null ) {
            return null;
        }

        PartnerResponse partnerResponse = new PartnerResponse();

        partnerResponse.setId( partner.getId() );
        partnerResponse.setCode( partner.getCode() );
        partnerResponse.setName( partner.getName() );
        partnerResponse.setSectorType( partner.getSectorType() );
        partnerResponse.setPlan( partner.getPlan() );
        partnerResponse.setStatus( partner.getStatus() );
        partnerResponse.setEmail( partner.getEmail() );
        partnerResponse.setPhone( partner.getPhone() );
        partnerResponse.setAddress( partner.getAddress() );
        partnerResponse.setTaxNumber( partner.getTaxNumber() );
        partnerResponse.setSubscriptionEnd( partner.getSubscriptionEnd() );
        partnerResponse.setCurrency( partner.getCurrency() );
        partnerResponse.setConfig( partner.getConfig() );
        partnerResponse.setCreatedAt( partner.getCreatedAt() );

        return partnerResponse;
    }
}
