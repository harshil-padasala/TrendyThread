package com.trendythread.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(name = "Notification", description = "A single in-app notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private Integer id;

    @Schema(description = "COMMENT, REPLY, or FOLLOW", example = "COMMENT")
    private String type;

    private String message;

    @Schema(description = "The related post's ID, if any. Null for FOLLOW notifications.", example = "42")
    private Integer postId;

    private boolean read;

    private LocalDateTime createdAt;
}
