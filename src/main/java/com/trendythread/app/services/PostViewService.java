package com.trendythread.app.services;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PostViewService {

    /** Increments the view count for a post. Fire-and-forget; failures shouldn't break the page load. */
    void recordView(Integer postId);

    /** Current view count for a post (0 if never viewed or Redis unavailable). */
    long getViewCount(Integer postId);

    /** Post IDs ordered by view count descending, most-viewed first. */
    List<Integer> getTrendingPostIds(int limit);
}
