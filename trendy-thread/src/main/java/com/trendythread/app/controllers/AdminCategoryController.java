package com.trendythread.app.controllers;

import com.trendythread.app.dto.AdminCategoryUpdateDto;
import com.trendythread.app.dto.CategoryDto;
import com.trendythread.app.dto.CategoryStatsDto;
import com.trendythread.app.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Admin Category Management",
        description = "Admin-only endpoints for managing featured categories, display order, and auto-update algorithms. Requires ROLE_ADMIN."
)
@RestController
@RequestMapping("/api/v1/admin/category")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Requires admin role
@SecurityRequirement(name = "Bearer Authentication")
public class AdminCategoryController {
    
    private final CategoryService categoryService;
    
    @Operation(
            summary = "Update Featured Status",
            description = "Updates the featured flag and display order for a specific category. " +
                    "This allows admins to manually control which categories appear in the navbar and their order. " +
                    "Display order determines the sequence in which featured categories are shown (1 = first, 2 = second, etc.)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Category featured status updated successfully",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Category not found with the provided ID",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token missing or invalid",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Requires ROLE_ADMIN",
                    content = @Content
            )
    })
    @PutMapping("/{categoryId}/featured")
    public ResponseEntity<CategoryDto> updateFeaturedStatus(
            @PathVariable Integer categoryId,
            @Valid @RequestBody AdminCategoryUpdateDto updateDto
    ) {
        CategoryDto updated = categoryService.updateCategoryFeaturedStatus(categoryId, updateDto);
        return ResponseEntity.ok(updated);
    }
    
    @Operation(
            summary = "Get Category Suggestions",
            description = "Retrieves a list of suggested categories based on the automated algorithm. " +
                    "Suggestions are ranked by popularity (post count) and help admins decide which categories to feature. " +
                    "Categories are marked with 'autoSuggested' flag if they meet the criteria for automatic featuring."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved category suggestions",
                    content = @Content(schema = @Schema(implementation = CategoryStatsDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token missing or invalid",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Requires ROLE_ADMIN",
                    content = @Content
            )
    })
    @GetMapping("/suggestions")
    public ResponseEntity<List<CategoryStatsDto>> getSuggestedCategories(
            @RequestParam(defaultValue = "15") int limit
    ) {
        List<CategoryStatsDto> suggestions = categoryService.getSuggestedCategories(limit);
        return ResponseEntity.ok(suggestions);
    }
    
    @Operation(
            summary = "Bulk Update Featured Categories",
            description = "Performs a bulk update to replace ALL featured categories with a new set in a single atomic operation. " +
                    "This method unfeatures all existing categories and features only the provided list. " +
                    "The order of category IDs in the array determines the display order (first item = displayOrder 1). " +
                    "All category IDs must be valid - if any ID is not found, the entire operation is rolled back. " +
                    "Commonly used for drag-and-drop reordering in admin UI."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Featured categories updated successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "One or more category IDs not found - no changes made",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token missing or invalid",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Requires ROLE_ADMIN",
                    content = @Content
            )
    })
    @PutMapping("/featured/bulk")
    public ResponseEntity<String> bulkUpdateFeaturedCategories(
            @RequestBody List<Integer> categoryIds
    ) {
        categoryService.bulkUpdateFeaturedCategories(categoryIds);
        return ResponseEntity.ok("Featured categories updated successfully");
    }
    
    @Operation(
            summary = "Trigger Auto-Update Algorithm",
            description = "Manually triggers the automated featured category update algorithm. " +
                    "This algorithm preserves manually featured categories (autoSuggested=false) and fills remaining slots (up to 10 total) " +
                    "with popular categories based on post count. The algorithm normally runs on a schedule (every 10 minutes), " +
                    "but this endpoint allows admins to trigger it immediately for testing or immediate refresh."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Auto-update algorithm completed successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token missing or invalid",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Requires ROLE_ADMIN",
                    content = @Content
            )
    })
    @PostMapping("/auto-update")
    public ResponseEntity<String> triggerAutoUpdate() {
        categoryService.autoUpdateFeaturedCategories();
        return ResponseEntity.ok("Auto-update completed successfully");
    }

    @Operation(
            summary = "Recalculate All Post Counts",
            description = "Manually triggers recalculation of post counts for ALL categories. " +
                    "This scans all posts in the database and updates each category's postCount field. " +
                    "Useful after data imports, migrations, or if post counts get out of sync. " +
                    "This operation may take a few seconds for large databases."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Post counts recalculated successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token missing or invalid",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Requires ROLE_ADMIN",
                    content = @Content
            )
    })
    @PostMapping("/recalculate-post-counts")
    public ResponseEntity<String> recalculatePostCounts() {
        categoryService.recalculateAllPostCounts();
        return ResponseEntity.ok("Post counts recalculated successfully for all categories");
    }
}