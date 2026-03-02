package com.trendythread.app.services.impl;

import com.trendythread.app.entities.Category;
import com.trendythread.app.exceptions.ResourceNotFoundException;
import com.trendythread.app.dto.CategoryDto;
import com.trendythread.app.repositories.CategoryRepository;
import com.trendythread.app.services.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryDto findByCategoryId(Integer categoryId) {
        log.info("findByCategoryId - request received: id={}", categoryId);
        Category category = this.categoryRepository.findById(categoryId).
                orElseThrow(() -> new ResourceNotFoundException("Category", "category id", categoryId));
        CategoryDto dto = this.categoryToCategoryDto(category);
        log.debug("findByCategoryId - fetched category: {}", dto);
        return dto;
    }

    @Override
    public List<CategoryDto> findAll() {
        log.info("findAll - request received");
        List<Category> categoryList = this.categoryRepository.findAll();
        List<CategoryDto> result = categoryList.stream().map((this::categoryToCategoryDto)).collect(Collectors.toList());
        log.debug("findAll - fetched {} categories", result.size());
        return result;
    }

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        log.info("createCategory - request received: {}", categoryDto);
        Category category = categoryDtoToCategory(categoryDto);
        Category savedCategory = this.categoryRepository.save(category);
        CategoryDto dto = this.categoryToCategoryDto(savedCategory);
        log.info("createCategory - created category id={}", dto.getCategoryId());
        return dto;
    }

    @Override
    public CategoryDto updateByCategoryId(Integer categoryId, CategoryDto categoryDto) {
        log.info("updateByCategoryId - request received: id={}, dto={}", categoryId, categoryDto);
        Category category = this.categoryRepository.findById(categoryId).
                orElseThrow(() -> new ResourceNotFoundException("Category", "category id", categoryId));
        category.setName(categoryDto.getCategoryName());
        category.setDescription(categoryDto.getDescription());

        Category savedCategory = this.categoryRepository.save(category);

        CategoryDto dto = this.categoryToCategoryDto(savedCategory);
        log.info("updateByCategoryId - update successful: id={}", dto.getCategoryId());
        return dto;
    }

    @Override
    public void deleteByCategoryId(Integer categoryId) {
        log.info("deleteByCategoryId - request received: id={}", categoryId);
        Category category = this.categoryRepository.findById(categoryId).
                orElseThrow(() -> new ResourceNotFoundException("Category", "category id", categoryId));
        this.categoryRepository.deleteById(categoryId);
        log.info("deleteByCategoryId - deleted category id={}", categoryId);
    }

    private CategoryDto categoryToCategoryDto(Category category) {
        return this.modelMapper.map(category, CategoryDto.class);
    }

    private Category categoryDtoToCategory(CategoryDto categoryDto) {
        return this.modelMapper.map(categoryDto, Category.class);
    }
}
