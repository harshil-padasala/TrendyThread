package com.trendythread.app.services;

import com.trendythread.app.dto.AdminCategoryUpdateDto;
import com.trendythread.app.dto.CategoryDto;
import com.trendythread.app.dto.CategoryStatsDto;
import com.trendythread.app.payloads.CategoryResponse;

import java.util.List;

public interface CategoryService {

    // Get Mapping
    CategoryDto findByCategoryId(Integer categoryId);

    // Get Mapping
    List<CategoryDto> findAll();

    // Post Mapping
    CategoryDto createCategory(CategoryDto categoryDto);

    // Put Mapping
    CategoryDto updateByCategoryId(Integer categoryId, CategoryDto categoryDto);

    // Delete Mapping
    void deleteByCategoryId(Integer categoryId);

    // Get featured categories for navbar (10-15 items)
    List<CategoryDto> getFeaturedCategories();

    // Get all categories with pagination for dedicated page
    CategoryResponse getAllCategoriesPaginated(int page, int size, String sortBy);

    // Search categories
    CategoryResponse searchCategories(String keyword, int page, int size);

    // Admin: Update featured status and display order
    CategoryDto updateCategoryFeaturedStatus(Integer categoryId, AdminCategoryUpdateDto updateDto);

    // Admin: Get suggested categories based on algorithm
    List<CategoryStatsDto> getSuggestedCategories(int limit);

    // Admin: Bulk update featured categories
    void bulkUpdateFeaturedCategories(List<Integer> categoryIds);

    // Update post counts (called when post is created/deleted)
    void updatePostCount(Integer categoryId);

    // Admin: Recalculate all category post counts
    void recalculateAllPostCounts();

    // Scheduled task: Auto-update featured categories
    void autoUpdateFeaturedCategories();
}
