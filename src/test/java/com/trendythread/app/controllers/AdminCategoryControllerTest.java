package com.trendythread.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trendythread.app.dto.AdminCategoryUpdateDto;
import com.trendythread.app.dto.CategoryDto;
import com.trendythread.app.dto.CategoryStatsDto;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Covers AdminCategoryController's endpoint wiring/response shapes. Role
 * enforcement itself ({@code @PreAuthorize("hasRole('ADMIN')")}) is AOP-based
 * method security and isn't active on a bare standalone-MockMvc instance
 * (no Spring context) — that's exercised at the SecurityConfig/integration
 * level instead, not here.
 */
@ExtendWith(MockitoExtension.class)
class AdminCategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private AdminCategoryController adminCategoryController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminCategoryController).build();
    }

    @Test
    void updateFeaturedStatusReturnsUpdatedCategory() throws Exception {
        AdminCategoryUpdateDto request = new AdminCategoryUpdateDto(true, 1);
        CategoryDto updated = new CategoryDto(1, "Cloud Computing", "A category about cloud technologies", true, 1, 5, false);
        when(categoryService.updateCategoryFeaturedStatus(eq(1), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/admin/category/1/featured")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.featured").value(true));
    }

    @Test
    void updateFeaturedStatusWithMissingFlagReturns400() throws Exception {
        mockMvc.perform(put("/api/v1/admin/category/1/featured")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSuggestedCategoriesReturnsList() throws Exception {
        when(categoryService.getSuggestedCategories(anyInt()))
                .thenReturn(List.of(new CategoryStatsDto(1L, "Cloud Computing", 12, 0, true)));

        mockMvc.perform(get("/api/v1/admin/category/suggestions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(categoryService).getSuggestedCategories(15);
    }

    @Test
    void bulkUpdateFeaturedCategoriesReturnsSuccessMessage() throws Exception {
        mockMvc.perform(put("/api/v1/admin/category/featured/bulk")
                        .contentType("application/json")
                        .content("[1,2,3]"))
                .andExpect(status().isOk());

        verify(categoryService).bulkUpdateFeaturedCategories(List.of(1, 2, 3));
    }

    @Test
    void triggerAutoUpdateReturnsSuccessMessage() throws Exception {
        mockMvc.perform(post("/api/v1/admin/category/auto-update"))
                .andExpect(status().isOk());

        verify(categoryService).autoUpdateFeaturedCategories();
    }

    @Test
    void recalculatePostCountsReturnsSuccessMessage() throws Exception {
        mockMvc.perform(post("/api/v1/admin/category/recalculate-post-counts"))
                .andExpect(status().isOk());

        verify(categoryService).recalculateAllPostCounts();
    }
}
