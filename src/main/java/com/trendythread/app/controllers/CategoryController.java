package com.trendythread.app.controllers;

import com.trendythread.app.payloads.ApiResponse;
import com.trendythread.app.dto.CategoryDto;
import com.trendythread.app.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        log.info("GET /api/v1/category/{} - findByCategoryId request received", categoryId);
        CategoryDto categoryDto = this.categoryService.findByCategoryId(categoryId);
        log.debug("GET /api/v1/category/{} - fetched category: {}", categoryId, categoryDto);
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
        log.info("GET /api/v1/category - fetchAllCategories request received");
        List<CategoryDto> categoryDtos = this.categoryService.findAll();
        log.debug("GET /api/v1/category - fetched {} categories", categoryDtos == null ? 0 : categoryDtos.size());
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
        log.info("POST /api/v1/category - createCategory request received: {}", categoryDto);
        CategoryDto categoryDto1 = this.categoryService.createCategory(categoryDto);
        log.info("POST /api/v1/category - created category: {}", categoryDto1);

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
        log.info("PUT /api/v1/category/{} - updateCategory request received: {}", categoryId, categoryDto);
        CategoryDto categoryDto1 = this.categoryService.updateByCategoryId(categoryId, categoryDto);
        log.info("PUT /api/v1/category/{} - update successful: {}", categoryId, categoryDto1);
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
        log.info("DELETE /api/v1/category/{} - deleteCategory request received", categoryId);
        this.categoryService.deleteByCategoryId(categoryId);
        log.info("DELETE /api/v1/category/{} - deletion completed", categoryId);
        ApiResponse apiResponse = new ApiResponse("category has been deleted", true);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

}
