package com.trendythread.app.controllers;

import com.trendythread.app.dto.FollowStatusDto;
import com.trendythread.app.services.FollowService;
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
        name = "Follows",
        description = "Follow/unfollow bloggers and check follow status"
)
@RestController
@RequestMapping("/api/v1/bloggers")
@Slf4j
public class FollowController {

    @Autowired
    private FollowService followService;

    @Operation(summary = "Toggle Follow", description = "Follows the blogger if not already followed, otherwise unfollows.")
    @PostMapping("/{bloggerId}/follow")
    public ResponseEntity<FollowStatusDto> toggleFollow(@PathVariable Integer bloggerId, Principal principal) {
        log.info("POST /api/v1/bloggers/{}/follow - toggleFollow request received: user={}", bloggerId, principal.getName());
        return ResponseEntity.ok(followService.toggleFollow(bloggerId, principal.getName()));
    }

    @Operation(summary = "Get Follow Status", description = "Returns the blogger's follower count and whether the requesting user follows them.")
    @GetMapping("/{bloggerId}/follow")
    public ResponseEntity<FollowStatusDto> getFollowStatus(@PathVariable Integer bloggerId, Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(followService.getFollowStatus(bloggerId, email));
    }
}
