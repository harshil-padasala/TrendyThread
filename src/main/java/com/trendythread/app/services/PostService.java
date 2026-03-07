package com.trendythread.app.services;

import com.trendythread.app.dto.PostDto;
import com.trendythread.app.payloads.PostResponse;
import org.springframework.stereotype.Service;

@Service
public interface PostService {

    // Get Mapping By ID
    PostDto findByPostId(Integer PostId);

    // Get Mapping
    PostResponse findAllPosts(Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc);

    // Get Latest Posts
    PostResponse getLatestPosts(Integer limit);

    // Get Mapping By All Category
    PostResponse findPostsByCategoryId(Integer categoryID, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc);

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
