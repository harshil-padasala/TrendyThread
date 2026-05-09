package com.trendythread.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatsDto {
    private Long categoryId;
    private String categoryName;
    private Integer postCount;
    private Integer totalViews; // Future enhancement
    private Boolean autoSuggested;
}