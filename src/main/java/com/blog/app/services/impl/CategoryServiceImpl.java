package com.blog.app.services.impl;

import com.blog.app.entities.Category;
import com.blog.app.exceptions.ResourceNotFoundException;
import com.blog.app.payloads.CategoryDto;
import com.blog.app.repositories.CategoryRepository;
import com.blog.app.services.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryDto getCategoryById(Integer categoryId) {
        Category category = this.categoryRepository.findById(categoryId).
                orElseThrow(() -> new ResourceNotFoundException("Category", "category id", categoryId));
        return this.categoryToCategoryDto(category);
    }

    @Override
    public List<CategoryDto> getAllCategory() {
        List<Category> categoryList = this.categoryRepository.findAll();
        return categoryList.stream().map((this::categoryToCategoryDto)).collect(Collectors.toList());
    }

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category = categoryDtoToCategory(categoryDto);
        Category savedCategory = this.categoryRepository.save(category);
        return this.categoryToCategoryDto(category);
    }

    @Override
    public CategoryDto updateCategoryById(Integer categoryId, CategoryDto categoryDto) {
        Category category = this.categoryRepository.findById(categoryId).
                orElseThrow(() -> new ResourceNotFoundException("Category", "category id", categoryId));
        category.setCategoryName(categoryDto.getCategoryName());
        category.setDescription(categoryDto.getDescription());

        this.categoryRepository.save(category);

        return this.categoryToCategoryDto(category);
    }

    @Override
    public void deleteCategoryById(Integer categoryId) {
        Category category = this.categoryRepository.findById(categoryId).
                orElseThrow(() -> new ResourceNotFoundException("Category", "category id", categoryId));
        this.categoryRepository.deleteById(categoryId);
    }

    private CategoryDto categoryToCategoryDto(Category category) {
        return this.modelMapper.map(category, CategoryDto.class);
    }

    private Category categoryDtoToCategory(CategoryDto categoryDto) {
        return this.modelMapper.map(categoryDto, Category.class);
    }
}
