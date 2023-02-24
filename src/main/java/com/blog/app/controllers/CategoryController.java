package com.blog.app.controllers;

import com.blog.app.entities.Category;
import com.blog.app.payloads.ApiResponse;
import com.blog.app.payloads.CategoryDto;
import com.blog.app.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/{categoryID}")
    public ResponseEntity<CategoryDto> findByID(@PathVariable Integer categoryID) {
        CategoryDto categoryDto = this.categoryService.getCategoryById(categoryID);
        return new ResponseEntity<>(categoryDto, HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<CategoryDto>> findAll() {
        List<CategoryDto> categoryDtos = this.categoryService.getAllCategory();
        return new ResponseEntity<>(categoryDtos, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto categoryDto) {
        CategoryDto categoryDto1 = this.categoryService.createCategory(categoryDto);

        return new ResponseEntity<>(categoryDto1, HttpStatus.CREATED);
    }

    @PutMapping("/{categoryID}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Integer categoryID, @Valid @RequestBody CategoryDto categoryDto) {
        CategoryDto categoryDto1 = this.categoryService.updateCategoryById(categoryID, categoryDto);
        return new ResponseEntity<>(categoryDto1, HttpStatus.CREATED);
    }

    @DeleteMapping("/{categoryID}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Integer categoryID) {
        this.categoryService.deleteCategoryById(categoryID);
        ApiResponse apiResponse = new ApiResponse("category has been deleted", true);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

}
