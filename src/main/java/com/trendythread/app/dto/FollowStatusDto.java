package com.trendythread.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(name = "FollowStatus", description = "Whether the current user follows a blogger, and that blogger's total follower count")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowStatusDto {

    @Schema(description = "Whether the requesting user follows this blogger.", example = "true")
    private boolean following;

    @Schema(description = "Total number of followers this blogger has.", example = "12")
    private long followerCount;
}
