package com.trendythread.app.controllers;

import com.trendythread.app.dto.LikeStatusDto;
import com.trendythread.app.services.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Tag(
        name = "Post Likes",
        description = "Like/unlike a post and check like status"
)
@RestController
@RequestMapping("/api/v1/posts")
@Slf4j
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Operation(
            summary = "Toggle Post Like",
            description = "Likes the post if the authenticated user hasn't liked it yet, otherwise unlikes it."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @PostMapping("/{postId}/like")
    public ResponseEntity<LikeStatusDto> toggleLike(@PathVariable Integer postId, Principal principal) {
        log.info("POST /api/v1/posts/{}/like - toggleLike request received: user={}", postId, principal.getName());
        LikeStatusDto status = likeService.toggleLike(postId, principal.getName());
        return ResponseEntity.ok(status);
    }

    @Operation(
            summary = "Get Post Like Status",
            description = "Returns the post's like count and whether the requesting user (if authenticated) has liked it."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping("/{postId}/like")
    public ResponseEntity<LikeStatusDto> getLikeStatus(@PathVariable Integer postId, Principal principal) {
        String email = principal != null ? principal.getName() : null;
        LikeStatusDto status = likeService.getLikeStatus(postId, email);
        return ResponseEntity.ok(status);
    }
}
