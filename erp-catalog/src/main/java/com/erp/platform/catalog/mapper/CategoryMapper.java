package com.erp.platform.catalog.mapper;

import com.erp.platform.catalog.dto.request.CreateCategoryRequest;
import com.erp.platform.catalog.dto.response.CategoryResponse;
import com.erp.platform.catalog.entity.Category;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface CategoryMapper {

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "productCount", ignore = true)
    @Mapping(target = "children", ignore = true)
    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);

    @Mapping(target = "active", constant = "true")
    Category toEntity(CreateCategoryRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(CreateCategoryRequest request, @MappingTarget Category category);
}
