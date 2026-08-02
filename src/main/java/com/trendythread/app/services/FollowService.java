package com.trendythread.app.services;

import com.trendythread.app.dto.FollowStatusDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FollowService {

    /** Follows the target blogger if not already followed, otherwise unfollows. */
    FollowStatusDto toggleFollow(Integer targetBloggerId, String followerEmail);

    /** Follower count for a blogger, and whether the given viewer (nullable) follows them. */
    FollowStatusDto getFollowStatus(Integer targetBloggerId, String viewerEmailOrNull);

    /** IDs of the bloggers this user follows, for building their personalized feed. */
    List<Integer> getFollowingIds(String email);
}
