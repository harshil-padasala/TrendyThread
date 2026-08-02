package com.trendythread.app.services;

import com.trendythread.app.dto.LikeStatusDto;
import org.springframework.stereotype.Service;

@Service
public interface LikeService {

    /**
     * Toggle the authenticated user's like on a post: likes it if not already
     * liked, unlikes it if already liked.
     */
    LikeStatusDto toggleLike(Integer postId, String authenticatedUserEmail);

    /**
     * Fetch the current like count for a post, and whether the given user
     * (nullable, for anonymous requests) has liked it.
     */
    LikeStatusDto getLikeStatus(Integer postId, String authenticatedUserEmailOrNull);
}
