package com.erp.platform.catalog.mapper;

import com.erp.platform.catalog.dto.request.CreateCategoryRequest;
import com.erp.platform.catalog.dto.response.CategoryResponse;
import com.erp.platform.catalog.entity.Category;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-29T15:49:24+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Amazon.com Inc.)"
)
@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public CategoryResponse toResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryResponse categoryResponse = new CategoryResponse();

        categoryResponse.setParentId( categoryParentId( category ) );
        categoryResponse.setParentName( categoryParentName( category ) );
        categoryResponse.setId( category.getId() );
        categoryResponse.setCode( category.getCode() );
        categoryResponse.setName( category.getName() );
        categoryResponse.setDescription( category.getDescription() );
        categoryResponse.setDisplayOrder( category.getDisplayOrder() );
        categoryResponse.setActive( category.isActive() );
        categoryResponse.setIconName( category.getIconName() );
        categoryResponse.setColorHex( category.getColorHex() );
        categoryResponse.setApplicableSector( category.getApplicableSector() );

        return categoryResponse;
    }

    @Override
    public List<CategoryResponse> toResponseList(List<Category> categories) {
        if ( categories == null ) {
            return null;
        }

        List<CategoryResponse> list = new ArrayList<CategoryResponse>( categories.size() );
        for ( Category category : categories ) {
            list.add( toResponse( category ) );
        }

        return list;
    }

    @Override
    public Category toEntity(CreateCategoryRequest request) {
        if ( request == null ) {
            return null;
        }

        Category category = new Category();

        category.setCode( request.getCode() );
        category.setName( request.getName() );
        category.setDescription( request.getDescription() );
        category.setDisplayOrder( request.getDisplayOrder() );
        category.setIconName( request.getIconName() );
        category.setColorHex( request.getColorHex() );
        category.setApplicableSector( request.getApplicableSector() );

        category.setActive( true );

        return category;
    }

    @Override
    public void updateEntityFromRequest(CreateCategoryRequest request, Category category) {
        if ( request == null ) {
            return;
        }

        if ( request.getCode() != null ) {
            category.setCode( request.getCode() );
        }
        if ( request.getName() != null ) {
            category.setName( request.getName() );
        }
        if ( request.getDescription() != null ) {
            category.setDescription( request.getDescription() );
        }
        category.setDisplayOrder( request.getDisplayOrder() );
        if ( request.getIconName() != null ) {
            category.setIconName( request.getIconName() );
        }
        if ( request.getColorHex() != null ) {
            category.setColorHex( request.getColorHex() );
        }
        if ( request.getApplicableSector() != null ) {
            category.setApplicableSector( request.getApplicableSector() );
        }
    }

    private UUID categoryParentId(Category category) {
        if ( category == null ) {
            return null;
        }
        Category parent = category.getParent();
        if ( parent == null ) {
            return null;
        }
        UUID id = parent.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String categoryParentName(Category category) {
        if ( category == null ) {
            return null;
        }
        Category parent = category.getParent();
        if ( parent == null ) {
            return null;
        }
        String name = parent.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
