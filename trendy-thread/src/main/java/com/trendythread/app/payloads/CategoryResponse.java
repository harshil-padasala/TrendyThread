package com.trendythread.app.payloads;

import com.trendythread.app.dto.CategoryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private List<CategoryDto> content;       // The actual categories
    private int pageNumber;                   // Current page (0-indexed)
    private int pageSize;                     // Items per page
    private long totalElements;               // Total number of categories
    private int totalPages;                   // Total pages available
    private boolean last;                     // Is this the last page?
}