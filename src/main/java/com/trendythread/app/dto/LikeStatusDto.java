package com.trendythread.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(
        name = "LikeStatus",
        description = "Whether the current user has liked a post, and the post's total like count"
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeStatusDto {

    @Schema(description = "Whether the requesting user has liked this post. False for anonymous requests.", example = "true")
    private boolean liked;

    @Schema(description = "Total number of likes on this post.", example = "12")
    private long likeCount;
}
