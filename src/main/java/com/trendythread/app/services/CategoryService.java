package com.trendythread.app.services;

import com.trendythread.app.dto.CategoryDto;

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
}
