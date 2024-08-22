package com.trendythread.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Schema(
        name = "Category",
        description = "Schema to hold Category information"
)

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {

    @Schema(
            description = "Unique identifier for the category.",
            example = "1"
    )
    private Integer categoryId;

    @Schema(
            description = "Name of the category. Must be at least 4 characters long.",
            example = "Cloud Computing"
    )
    @NotEmpty
    @Size(min = 4, max = 30, message = "size must be between minimum 4 characters long")
    private String categoryName;

    @Schema(
            description = "Detailed description of the category. Must be at least 10 characters long.",
            example = "A category that encompasses various cloud computing services and technologies."
    )
    @NotEmpty
    @Size(min = 10, max = 200, message = "size must be between minimum 4 characters long")
    private String description;
}
