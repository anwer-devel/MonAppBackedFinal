package com.app.category.controller;

import com.app.category.dto.*;
import com.app.category.entity.Category;
import com.app.category.service.CategoryService;
import com.app.common.response.ApiResponse;
import com.app.common.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management for quiz, questions, spinners")
public class CategoryController {

    private final CategoryService categoryService;

    // ===== ADMIN ROUTES =====

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create Public Category (Admin Only)",
            description = "Create a public category that is automatically approved"
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Category created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Admin role required"
            )
    })
    public ResponseEntity<ApiResponse<CategoryDto>> createPublicCategory(
            @Valid @RequestBody CreateCategoryDto dto) {
        CategoryDto category = categoryService.createPublicCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(category, "Public category created successfully"));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get All Categories (Admin Only)",
            description = "Retrieve all categories with filtering and pagination"
    )
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<ApiResponse<Page<CategoryDto>>> getAllCategories(
            @Parameter(description = "Category type filter") @RequestParam(required = false) String type,
            @Parameter(description = "Visibility filter") @RequestParam(required = false) String visibility,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Partner ID filter") @RequestParam(required = false) UUID partnerId,
            @Parameter(description = "Search term") @RequestParam(required = false) String search,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int limit) {

        FilterCategoryDto filterDto = FilterCategoryDto.builder()
                .page(page)
                .limit(limit)
                .search(search)
                .partnerId(partnerId)
                .build();

        Page<CategoryDto> categories = categoryService.getAllCategories(filterDto);
        return ResponseEntity.ok(ApiResponse.success(categories, "Categories retrieved successfully"));
    }

    @PatchMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Approve or Reject Category (Admin Only)",
            description = "Approve or reject a pending category with optional rejection reason"
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Category status updated"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Category not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid status or category not in pending"
            )
    })
    public ResponseEntity<ApiResponse<CategoryDto>> approveCategory(
            @PathVariable UUID id,
            @Valid @RequestBody ApproveCategoryDto dto) {
        CategoryDto category = categoryService.approveCategory(id, dto);
        return ResponseEntity.ok(ApiResponse.success(category, "Category status updated successfully"));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete Category (Admin Only)",
            description = "Delete a category (soft delete)"
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Category deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Category not found"
            )
    })
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // ===== PARTNER ROUTES =====

    @PostMapping("/partner")
    @PreAuthorize("hasRole('PARTNER_OWNER')")
    @Operation(
            summary = "Create Partner Category (Partner Only)",
            description = "Create a category for your establishment (requires approval)"
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Category created and pending approval"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Partner role required"
            )
    })
    public ResponseEntity<ApiResponse<CategoryDto>> createPartnerCategory(
            @Valid @RequestBody CreateCategoryDto dto) {
        UUID partnerId = SecurityUtil.getCurrentUserId();
        CategoryDto category = categoryService.createPartnerCategory(partnerId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(category, "Category created and pending approval"));
    }

    @GetMapping("/partner/mine")
    @PreAuthorize("hasRole('PARTNER_OWNER')")
    @Operation(
            summary = "Get My Categories (Partner Only)",
            description = "Retrieve all categories for your establishment"
    )
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<ApiResponse<Page<CategoryDto>>> getMyCategories(
            @Parameter(description = "Category type filter") @RequestParam(required = false) String type,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int limit) {

        UUID partnerId = SecurityUtil.getCurrentUserId();
        FilterCategoryDto filterDto = FilterCategoryDto.builder()
                .page(page)
                .limit(limit)
                .build();

        Page<CategoryDto> categories = categoryService.getMyCategories(partnerId, filterDto);
        return ResponseEntity.ok(ApiResponse.success(categories, "Your categories retrieved successfully"));
    }

    @PatchMapping("/partner/{id}")
    @PreAuthorize("hasRole('PARTNER_OWNER')")
    @Operation(
            summary = "Update My Category (Partner Only)",
            description = "Update your category (only if still in draft/pending status)"
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Category updated successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Not the owner of this category or cannot update"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Cannot update approved/rejected categories"
            )
    })
    public ResponseEntity<ApiResponse<CategoryDto>> updateMyCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryDto dto) {
        UUID partnerId = SecurityUtil.getCurrentUserId();
        CategoryDto category = categoryService.updateMyCategory(partnerId, id, dto);
        return ResponseEntity.ok(ApiResponse.success(category, "Category updated successfully"));
    }

    @DeleteMapping("/partner/{id}")
    @PreAuthorize("hasRole('PARTNER_OWNER')")
    @Operation(
            summary = "Delete My Category (Partner Only)",
            description = "Delete your category (soft delete)"
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Category deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Not the owner of this category"
            )
    })
    public ResponseEntity<Void> deleteMyCategory(@PathVariable UUID id) {
        UUID partnerId = SecurityUtil.getCurrentUserId();
        categoryService.deleteMyCategory(partnerId, id);
        return ResponseEntity.noContent().build();
    }

    // ===== CONTENT ROUTES =====

    @PostMapping("/{categoryId}/content")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PARTNER_OWNER')")
    @Operation(
            summary = "Add Content to Category",
            description = "Add quiz/question/spinner content to a category"
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Content added successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Category not found"
            )
    })
    public ResponseEntity<ApiResponse<CategoryContentDto>> addContent(
            @PathVariable UUID categoryId,
            @Valid @RequestBody CreateCategoryContentDto dto) {
        CategoryContentDto content = categoryService.addContent(categoryId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(content, "Content added successfully"));
    }

    @PatchMapping("/content/{contentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PARTNER_OWNER')")
    @Operation(
            summary = "Update Category Content",
            description = "Update quiz/question/spinner content"
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Content updated successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Content not found"
            )
    })
    public ResponseEntity<ApiResponse<CategoryContentDto>> updateContent(
            @PathVariable UUID contentId,
            @Valid @RequestBody UpdateCategoryContentDto dto) {
        CategoryContentDto content = categoryService.updateContent(contentId, dto);
        return ResponseEntity.ok(ApiResponse.success(content, "Content updated successfully"));
    }

    @DeleteMapping("/content/{contentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PARTNER_OWNER')")
    @Operation(
            summary = "Delete Category Content",
            description = "Delete quiz/question/spinner content"
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Content deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Content not found"
            )
    })
    public ResponseEntity<Void> deleteContent(@PathVariable UUID contentId) {
        categoryService.deleteContent(contentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{categoryId}/content/reorder")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PARTNER_OWNER')")
    @Operation(
            summary = "Reorder Category Content",
            description = "Reorder contents in a category"
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Content reordered successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Category not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid content IDs"
            )
    })
    public ResponseEntity<Void> reorderContent(
            @PathVariable UUID categoryId,
            @RequestBody List<UUID> orderedIds) {
        categoryService.reorderContent(categoryId, orderedIds);
        return ResponseEntity.noContent().build();
    }

    // ===== FILTERED LIST FOR ADMIN/PARTNER =====

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PARTNER_OWNER')")
    @Operation(
            summary = "Get Categories by Status",
            description = "Retrieve categories filtered by status (for event creation)"
    )
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<ApiResponse<Page<CategoryDto>>> getCategoriesByStatus(
            @Parameter(description = "Status filter (APPROVED, PENDING_APPROVAL, REJECTED)") @RequestParam(required = false, defaultValue = "APPROVED") String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int limit) {

        // FIX: Validation du status avec gestion d'erreur explicite
        Category.CategoryStatus categoryStatus;
        try {
            categoryStatus = Category.CategoryStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new com.app.common.exception.BadRequestException(
                    "Invalid status: " + status + ". Valid values: APPROVED, PENDING_APPROVAL, REJECTED, DRAFT");
        }

        FilterCategoryDto filterDto = FilterCategoryDto.builder()
                .page(page)
                .limit(limit)
                .status(categoryStatus)
                .build();

        Page<CategoryDto> categories = categoryService.getAllCategories(filterDto);
        return ResponseEntity.ok(ApiResponse.success(categories, "Categories retrieved successfully"));
    }

    // ===== PUBLIC ROUTES =====

    @GetMapping("/partner/{partnerId}/public")
    @PreAuthorize("hasRole('USER') or hasRole('PARTNER_OWNER') or hasRole('ADMIN')")
    @Operation(
            summary = "Get Public Categories by Partner",
            description = "Retrieve public and approved categories for a specific partner"
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Categories retrieved successfully"
            )
    })
    public ResponseEntity<ApiResponse<Page<CategoryDto>>> getPublicCategoriesByPartner(
            @PathVariable UUID partnerId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int limit) {

        FilterCategoryDto filterDto = FilterCategoryDto.builder()
                .page(page)
                .limit(limit)
                .build();

        Page<CategoryDto> categories = categoryService.getPublicCategoriesByPartner(partnerId, filterDto);
        return ResponseEntity.ok(ApiResponse.success(categories, "Public categories retrieved successfully"));
    }

    @GetMapping("/{categoryId}")
    @PreAuthorize("hasRole('USER') or hasRole('PARTNER_OWNER') or hasRole('ADMIN')")
    @Operation(
            summary = "Get Category with Full Content",
            description = "Retrieve category details with all its contents"
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Category retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Category not found"
            )
    })
    public ResponseEntity<ApiResponse<CategoryWithContentDto>> getCategoryWithContent(
            @PathVariable UUID categoryId) {
        CategoryWithContentDto category = categoryService.getCategoryWithContent(categoryId);
        return ResponseEntity.ok(ApiResponse.success(category, "Category retrieved successfully"));
    }
}

