package com.trendythread.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Schema(
        name = "Post",
        description = "Schema to hold Post Details"
)

@Setter
@Getter
@NoArgsConstructor
public class PostDto {

    @Schema(
            description = "Unique identifier for the post.",
            example = "1"
    )
    private Integer id;

    @Schema(
            description = "Title of the post. Must be at least 4 characters long.",
            example = "Introduction to Cloud Computing"
    )
    @NotEmpty
    @Size(min = 4, message = "size must be between minimum 4 characters long")
    private String title;

    @Schema(
            description = "Description of the post. Must be at least 10 characters long.",
            example = "Cloud computing is a technology that allows users to access and manage computing resources over the internet..."
    )
    @NotEmpty
    @Size(min = 10, message = "size must be between minimum 10 characters long")
    private String description;

    @Schema(
            description = "Content of the post. Must be at least 10 characters long."
    )
    @NotEmpty
    @Size(min = 10, message = "size must be between minimum 10 characters long")
    private String content;

    @Schema(
            description = "Date and time when the post was created.",
            example = "2024-08-21T14:30:00Z"
    )
    private Date createdDate;

    @Schema(
            description = "Category associated with the post.",
            implementation = CategoryDto.class
    )
    private CategoryDto category;

    @Schema(
            description = "User who created the post.",
            implementation = UserDto.class
    )
    private UserDto user;

    @Schema(
            description = "Set of comments associated with the post.",
            implementation = CommentDto.class
    )
    private Set<CommentDto> comments = new HashSet<>();
}

