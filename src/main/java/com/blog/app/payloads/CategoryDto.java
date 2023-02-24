package com.blog.app.payloads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {

    private Integer categoryId;

    @NotEmpty
    @Size(min = 4, message = "size must be between minimum 4 characters long")
    private String categoryName;

    @NotEmpty
    @Size(min = 10, message = "size must be between minimum 4 characters long")
    private String description;
}
