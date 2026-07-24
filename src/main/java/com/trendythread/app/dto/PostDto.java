package com.trendythread.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
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
            description = "Title of the post. Must be between 4 and 500 characters.",
            example = "Introduction to Cloud Computing"
    )
    @NotEmpty(message = "Title cannot be empty")
    @Size(min = 4, max = 500, message = "Title must be between 4 and 500 characters")
    private String title;

    @Schema(
            description = "Description of the post. Must be between 10 and 1000 characters.",
            example = "Cloud computing is a technology that allows bloggers to access and manage computing resources over the internet..."
    )
    @NotEmpty(message = "Description cannot be empty")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    @Schema(
            description = "Content of the post. Must be at least 10 characters long."
    )
    @NotEmpty(message = "Content cannot be empty")
    @Size(min = 10, message = "Content must be at least 10 characters long")
    private String content;

    @Schema(
            description = "Date and time when the post was created.",
            example = "2024-08-21T14:30:00Z"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Date and time when the post was last updated.",
            example = "2024-08-22T10:15:00Z"
    )
    private LocalDateTime updatedAt;

    @Schema(
            description = "Category associated with the post.",
            implementation = CategoryDto.class
    )
    private CategoryDto category;

    @Schema(
            description = "Blogger who created the post.",
            implementation = BloggerDto.class
    )
    private BloggerDto blogger;

    @Schema(
            description = "Set of comments associated with the post.",
            implementation = CommentDto.class
    )
    private Set<CommentDto> comments = new HashSet<>();
}

