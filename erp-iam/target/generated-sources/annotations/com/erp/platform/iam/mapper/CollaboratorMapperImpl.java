package com.erp.platform.iam.mapper;

import com.erp.platform.iam.dto.collaborator.CollaboratorResponse;
import com.erp.platform.iam.entity.Collaborator;
import com.erp.platform.iam.entity.LocalUnit;
import com.erp.platform.iam.entity.Partner;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-29T15:49:26+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Amazon.com Inc.)"
)
@Component
public class CollaboratorMapperImpl implements CollaboratorMapper {

    @Override
    public CollaboratorResponse toResponse(Collaborator collaborator) {
        if ( collaborator == null ) {
            return null;
        }

        CollaboratorResponse.CollaboratorResponseBuilder collaboratorResponse = CollaboratorResponse.builder();

        collaboratorResponse.partnerId( collaboratorPartnerId( collaborator ) );
        collaboratorResponse.partnerName( collaboratorPartnerName( collaborator ) );
        collaboratorResponse.defaultLocalId( collaboratorDefaultLocalId( collaborator ) );
        collaboratorResponse.defaultLocalName( collaboratorDefaultLocalName( collaborator ) );
        collaboratorResponse.id( collaborator.getId() );
        collaboratorResponse.email( collaborator.getEmail() );
        collaboratorResponse.firstName( collaborator.getFirstName() );
        collaboratorResponse.lastName( collaborator.getLastName() );
        collaboratorResponse.phone( collaborator.getPhone() );
        collaboratorResponse.role( collaborator.getRole() );
        collaboratorResponse.status( collaborator.getStatus() );
        collaboratorResponse.multiLocal( collaborator.isMultiLocal() );
        List<UUID> list = collaborator.getLocalAccess();
        if ( list != null ) {
            collaboratorResponse.localAccess( new ArrayList<UUID>( list ) );
        }
        collaboratorResponse.lastLoginAt( collaborator.getLastLoginAt() );
        collaboratorResponse.createdAt( collaborator.getCreatedAt() );

        return collaboratorResponse.build();
    }

    private UUID collaboratorPartnerId(Collaborator collaborator) {
        if ( collaborator == null ) {
            return null;
        }
        Partner partner = collaborator.getPartner();
        if ( partner == null ) {
            return null;
        }
        UUID id = partner.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String collaboratorPartnerName(Collaborator collaborator) {
        if ( collaborator == null ) {
            return null;
        }
        Partner partner = collaborator.getPartner();
        if ( partner == null ) {
            return null;
        }
        String name = partner.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private UUID collaboratorDefaultLocalId(Collaborator collaborator) {
        if ( collaborator == null ) {
            return null;
        }
        LocalUnit defaultLocal = collaborator.getDefaultLocal();
        if ( defaultLocal == null ) {
            return null;
        }
        UUID id = defaultLocal.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String collaboratorDefaultLocalName(Collaborator collaborator) {
        if ( collaborator == null ) {
            return null;
        }
        LocalUnit defaultLocal = collaborator.getDefaultLocal();
        if ( defaultLocal == null ) {
            return null;
        }
        String name = defaultLocal.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
