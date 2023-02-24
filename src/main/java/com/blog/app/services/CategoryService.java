package com.blog.app.services;

import com.blog.app.payloads.CategoryDto;
import org.springframework.stereotype.Service;

import java.util.List;

public interface CategoryService {

    // Get Mapping
    CategoryDto getCategoryById(Integer categoryId);

    // Get Mapping
    List<CategoryDto> getAllCategory();

    // Post Mapping
    CategoryDto createCategory(CategoryDto categoryDto);

    // Put Mapping
    CategoryDto updateCategoryById(Integer categoryId, CategoryDto categoryDto);

    // Delete Mapping
    void deleteCategoryById(Integer categoryId);
}
