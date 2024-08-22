package com.trendythread.app.controllers;

import com.trendythread.app.payloads.ApiResponse;
import com.trendythread.app.dto.CategoryDto;
import com.trendythread.app.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Tag(
        name = "CRUD REST APIs for CATEGORY in TrendyThread",
        description = "CRUD REST APIs in TrendyThread to CREATE, UPDATE, FETCH and DELETE category details"
)
@RestController
@RequestMapping("/api/v1/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Operation(
            summary = "FETCH Category REST API",
            description = "REST API to fetch category based on category-id"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> findByCategoryId(@PathVariable Integer categoryId) {
        CategoryDto categoryDto = this.categoryService.findByCategoryId(categoryId);
        return new ResponseEntity<>(categoryDto, HttpStatus.OK);
    }

    @Operation(
            summary = "FETCH Category REST API",
            description = "REST API to fetch all categories in TrendyThread"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping
    public ResponseEntity<List<CategoryDto>> fetchAllCategories() {
        List<CategoryDto> categoryDtos = this.categoryService.findAll();
        return new ResponseEntity<>(categoryDtos, HttpStatus.OK);
    }

    @Operation(
            summary = "CREATE Category REST API",
            description = "REST API to create a new Category in TrendyThread"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "HTTP Status CREATED"
    )
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto categoryDto) {
        CategoryDto categoryDto1 = this.categoryService.createCategory(categoryDto);

        return new ResponseEntity<>(categoryDto1, HttpStatus.CREATED);
    }

    @Operation(
            summary = "UPDATE Category REST API",
            description = "REST API to update category details based on category-id"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Integer categoryId, @Valid @RequestBody CategoryDto categoryDto) {
        CategoryDto categoryDto1 = this.categoryService.updateByCategoryId(categoryId, categoryDto);
        return new ResponseEntity<>(categoryDto1, HttpStatus.CREATED);
    }

    @Operation(
            summary = "DELETE Category REST API",
            description = "REST API to delete category details based on category-id"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Integer categoryId) {
        this.categoryService.deleteByCategoryId(categoryId);
        ApiResponse apiResponse = new ApiResponse("category has been deleted", true);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

}
