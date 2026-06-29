package com.erp.platform.catalog.mapper;

import com.erp.platform.catalog.dto.request.CreateUnitRequest;
import com.erp.platform.catalog.dto.response.UnitResponse;
import com.erp.platform.catalog.entity.Unit;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-29T15:49:24+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Amazon.com Inc.)"
)
@Component
public class UnitMapperImpl implements UnitMapper {

    @Override
    public UnitResponse toResponse(Unit unit) {
        if ( unit == null ) {
            return null;
        }

        UnitResponse unitResponse = new UnitResponse();

        unitResponse.setId( unit.getId() );
        unitResponse.setCode( unit.getCode() );
        unitResponse.setName( unit.getName() );
        unitResponse.setSymbol( unit.getSymbol() );
        unitResponse.setType( unit.getType() );
        unitResponse.setDefault( unit.isDefault() );

        return unitResponse;
    }

    @Override
    public List<UnitResponse> toResponseList(List<Unit> units) {
        if ( units == null ) {
            return null;
        }

        List<UnitResponse> list = new ArrayList<UnitResponse>( units.size() );
        for ( Unit unit : units ) {
            list.add( toResponse( unit ) );
        }

        return list;
    }

    @Override
    public Unit toEntity(CreateUnitRequest request) {
        if ( request == null ) {
            return null;
        }

        Unit unit = new Unit();

        unit.setCode( request.getCode() );
        unit.setName( request.getName() );
        unit.setSymbol( request.getSymbol() );
        unit.setType( request.getType() );

        unit.setDefault( false );

        return unit;
    }

    @Override
    public void updateEntityFromRequest(CreateUnitRequest request, Unit unit) {
        if ( request == null ) {
            return;
        }

        if ( request.getCode() != null ) {
            unit.setCode( request.getCode() );
        }
        if ( request.getName() != null ) {
            unit.setName( request.getName() );
        }
        if ( request.getSymbol() != null ) {
            unit.setSymbol( request.getSymbol() );
        }
        if ( request.getType() != null ) {
            unit.setType( request.getType() );
        }
    }
}
