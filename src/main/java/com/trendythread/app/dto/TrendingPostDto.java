package com.trendythread.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(name = "TrendingPost", description = "A post paired with its view count, for trending listings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendingPostDto {

    private PostDto post;

    @Schema(description = "Total number of times this post has been viewed.", example = "128")
    private long viewCount;
}
