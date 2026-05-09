package com.trendythread.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCategoryUpdateDto {
    
    @NotNull(message = "Featured flag is required")
    private Boolean featured;
    
    @Min(value = 0, message = "Display order must be 0 or greater")
    private Integer displayOrder;
}