package com.blog.app.payloads;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
public class PostDto {

    private Integer postID;

    @NotEmpty
    @Size(min = 4, message = "size must be between minimum 4 characters long")
    private String title;

    @NotEmpty
    @Size(min = 10, message = "size must be between minimum 4 characters long")
    private String content;

    private String imageName;

    private Date createdDate;

    private CategoryDto category;

    private UserDto user;
}

