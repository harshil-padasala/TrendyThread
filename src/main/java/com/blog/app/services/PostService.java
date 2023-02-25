package com.blog.app.services;

import com.blog.app.payloads.PostDto;
import com.blog.app.payloads.PostResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PostService {

    // Get Mapping By ID
    PostDto getPostByID(Integer PostID);

    // Get Mapping
    PostResponse getAllPosts(Integer pageNumber, Integer pageSize);

    // Get Mapping By All Category
    PostResponse getAllPostsByCategory(Integer categoryID, Integer pageNumber, Integer pageSize);

    // Get Mapping By All User
    PostResponse getAllPostsByUser(Integer userID, Integer pageNumber, Integer pageSize);

    // Post Mapping
    PostDto createPost(PostDto postDto, Integer userId, Integer postId);

    // Put Mapping
    PostDto updatePostById(Integer postID, PostDto postDto);

    // Delete Mapping
    void deletePostByID(Integer postID);
}
