package com.trendythread.app.services;

import com.trendythread.app.dto.PostDto;
import com.trendythread.app.dto.PostViewCountDto;
import com.trendythread.app.dto.TrendingPostDto;
import com.trendythread.app.payloads.PostResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PostService {

    // Get Mapping By ID
    PostDto findByPostId(Integer PostId);

    // View count for a single post
    PostViewCountDto getViewCount(Integer postId);

    // Most-viewed posts, view count included
    List<TrendingPostDto> getTrendingPosts(int limit);

    // Get Mapping
    PostResponse findAllPosts(Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc);

    // Get Latest Posts
    PostResponse getLatestPosts(Integer limit);

    // Get Mapping By All Category
    PostResponse findPostsByCategoryId(Integer categoryID, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc);

    // Get Mapping By Tag
    PostResponse findPostsByTag(String tagName, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc);

    // Personalized feed: posts from bloggers the user follows
    PostResponse getFeed(String authenticatedUserEmail, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc);

    // Get Mapping By All Blogger
    PostResponse findPostsByBloggerId(String authenticatedUserEmail, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc);

    // Get Mapping By User ID
    PostResponse findPostsByUserId(Integer userId, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc);

    // Post Mapping
    PostDto createPost(PostDto postDto, String authenticatedUserEmail, Integer postId);

    // Put Mapping
    PostDto updateByPostId(Integer postID, PostDto postDto, String authenticatedUserEmail);

    // Delete Mapping
    void deleteByPostId(Integer postID);

    // search method
    PostResponse searchPost(String keyword, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc);
}
