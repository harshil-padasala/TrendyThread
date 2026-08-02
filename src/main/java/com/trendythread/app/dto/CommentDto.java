package com.trendythread.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Schema(
        name = "Comment",
        description = "Schema to hold Comment Details"
)

@Getter
@Setter
public class CommentDto {

    @Schema(
            description = "Unique identifier for the comment.",
            example = "1"
    )
    private Integer id;

    @NotEmpty(message = "Name should not be null or empty")
    private String name;

    @NotEmpty(message = "Email should not be null or empty")
    @Email
    private String email;


    @Schema(
            description = "Content of the comment.",
            example = "This is a very insightful post. Thanks for sharing!"
    )
    @NotEmpty
    @Size(min = 10, message = "Comment body must be minimum 10 characters")
    private String content;

    @Schema(
            description = "ID of the comment this is a reply to. Omit/null for a top-level comment.",
            example = "3"
    )
    private Integer parentId;

    @Schema(
            description = "Nested replies to this comment, in reply order. Populated only in read responses."
    )
    private List<CommentDto> replies = new ArrayList<>();
}
