package com.erp.platform.catalog.service;

import com.erp.platform.catalog.dto.request.CreateCategoryRequest;
import com.erp.platform.catalog.dto.response.CategoryResponse;
import com.erp.platform.catalog.entity.Category;
import com.erp.platform.catalog.mapper.CategoryMapper;
import com.erp.platform.catalog.repository.CategoryRepository;
import com.erp.platform.core.exception.ConflictException;
import com.erp.platform.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponse> getTree() {
        List<Category> roots = categoryRepository.findByParentIsNullAndIsActiveTrueAndIsDeletedFalse();
        return roots.stream()
                .map(this::mapToTreeResponse)
                .collect(Collectors.toList());
    }

    private CategoryResponse mapToTreeResponse(Category category) {
        CategoryResponse resp = categoryMapper.toResponse(category);
        resp.setProductCount(0);
        if (category.getChildren() != null) {
            List<CategoryResponse> childrenResp = category.getChildren().stream()
                    .filter(c -> !c.isDeleted() && c.isActive())
                    .map(this::mapToTreeResponse)
                    .collect(Collectors.toList());
            resp.setChildren(childrenResp);
        }
        return resp;
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest req) {
        if (categoryRepository.findByCodeIgnoreCaseAndIsDeletedFalse(req.getCode()).isPresent()) {
            throw new ConflictException("Le code catégorie '" + req.getCode() + "' est déjà utilisé", "code");
        }

        Category parent = null;
        if (req.getParentId() != null) {
            parent = categoryRepository.findById(req.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", req.getParentId()));
            if (parent.getParent() != null && parent.getParent().getParent() != null) {
                throw new ConflictException("Profondeur maximale de catégorie (3) atteinte", "parentId");
            }
        }

        Category entity = categoryMapper.toEntity(req);
        entity.setParent(parent);
        Category saved = categoryRepository.save(entity);
        return categoryMapper.toResponse(saved);
    }

    @Transactional
    public CategoryResponse update(UUID id, CreateCategoryRequest req) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (!category.getCode().equalsIgnoreCase(req.getCode())) {
            if (categoryRepository.findByCodeIgnoreCaseAndIsDeletedFalse(req.getCode()).isPresent()) {
                throw new ConflictException("Le code catégorie '" + req.getCode() + "' est déjà utilisé", "code");
            }
        }

        Category parent = null;
        if (req.getParentId() != null) {
            if (req.getParentId().equals(id)) {
                throw new ConflictException("Une catégorie ne peut pas être son propre parent", "parentId");
            }
            parent = categoryRepository.findById(req.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", req.getParentId()));
        }

        categoryMapper.updateEntityFromRequest(req, category);
        category.setParent(parent);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Transactional
    public void toggleActive(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        category.setActive(!category.isActive());
        categoryRepository.save(category);
    }
}
