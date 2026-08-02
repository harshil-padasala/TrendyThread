package com.trendythread.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trendythread.app.dto.CategoryDto;
import com.trendythread.app.exceptions.GlobalExceptionHandler;
import com.trendythread.app.exceptions.ResourceNotFoundException;
import com.trendythread.app.payloads.CategoryResponse;
import com.trendythread.app.services.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private CategoryDto validCategoryDto() {
        return new CategoryDto(null, "Cloud Computing", "A category about cloud technologies", false, 0, 0, false);
    }

    @Test
    void findByCategoryIdReturnsCategory() throws Exception {
        CategoryDto dto = validCategoryDto();
        dto.setCategoryId(1);
        when(categoryService.findByCategoryId(1)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(1));
    }

    @Test
    void findByCategoryIdNotFoundReturns404() throws Exception {
        when(categoryService.findByCategoryId(999)).thenThrow(new ResourceNotFoundException("Category", "categoryId", 999));

        mockMvc.perform(get("/api/v1/category/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void fetchAllCategoriesReturnsList() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of(validCategoryDto()));

        mockMvc.perform(get("/api/v1/category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void createCategoryReturns201() throws Exception {
        CategoryDto request = validCategoryDto();
        CategoryDto saved = validCategoryDto();
        saved.setCategoryId(1);
        when(categoryService.createCategory(any(CategoryDto.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/category")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").value(1));
    }

    @Test
    void createCategoryWithBlankNameReturns400() throws Exception {
        CategoryDto request = validCategoryDto();
        request.setCategoryName("");

        mockMvc.perform(post("/api/v1/category")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCategoryReturnsUpdated() throws Exception {
        CategoryDto request = validCategoryDto();
        CategoryDto updated = validCategoryDto();
        updated.setCategoryId(1);
        when(categoryService.updateByCategoryId(eq(1), any(CategoryDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/category/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.categoryId").value(1));
    }

    @Test
    void deleteCategoryReturnsSuccessResponse() throws Exception {
        mockMvc.perform(delete("/api/v1/category/1"))
                .andExpect(jsonPath("$.success").value(true));

        verify(categoryService).deleteByCategoryId(1);
    }

    @Test
    void getFeaturedCategoriesReturnsList() throws Exception {
        when(categoryService.getFeaturedCategories()).thenReturn(List.of(validCategoryDto()));

        mockMvc.perform(get("/api/v1/category/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllCategoriesPaginatedReturnsResponse() throws Exception {
        CategoryResponse response = CategoryResponse.builder()
                .content(List.of(validCategoryDto()))
                .pageNumber(0)
                .pageSize(20)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();
        when(categoryService.getAllCategoriesPaginated(anyInt(), anyInt(), anyString())).thenReturn(response);

        mockMvc.perform(get("/api/v1/category/paginated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchCategoriesReturnsResponse() throws Exception {
        CategoryResponse response = CategoryResponse.builder()
                .content(List.of())
                .pageNumber(0)
                .pageSize(20)
                .totalElements(0)
                .totalPages(0)
                .last(true)
                .build();
        when(categoryService.searchCategories(eq("cloud"), anyInt(), anyInt())).thenReturn(response);

        mockMvc.perform(get("/api/v1/category/search").param("keyword", "cloud"))
                .andExpect(status().isOk());
    }
}
