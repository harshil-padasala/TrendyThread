package com.trendythread.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(name = "PostViewCount", description = "Total view count for a post")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostViewCountDto {

    @Schema(description = "Total number of times this post has been viewed.", example = "128")
    private long viewCount;
}
