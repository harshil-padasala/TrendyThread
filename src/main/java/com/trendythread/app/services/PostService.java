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

    // Get Mapping By All Category
    PostResponse findPostsByCategoryId(Integer categoryID, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc);

    // Get Mapping By All User
    PostResponse findPostsByUserId(Integer userID, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc);

    // Post Mapping
    PostDto createPost(PostDto postDto, Integer userId, Integer postId);

    // Put Mapping
    PostDto updateByPostId(Integer postID, PostDto postDto);

    // Delete Mapping
    void deleteByPostId(Integer postID);

    // search method
    PostResponse searchPost(String keyword, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc);
}
