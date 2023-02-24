package com.blog.app.services;

import com.blog.app.payloads.PostDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PostService {

    // Get Mapping By ID
    PostDto getPostByID(Integer PostID);

    // Get Mapping
    List<PostDto> getAllPosts();

    // Get Mapping By All Category
    List<PostDto> getAllPostsByCategory(Integer categoryID);

    // Get Mapping By All User
    List<PostDto> getAllPostsByUser(Integer userID);

    // Post Mapping
    PostDto createPost(PostDto postDto, Integer userId, Integer postId);

    // Put Mapping
    PostDto updatePostById(Integer postID, PostDto postDto);

    // Delete Mapping
    void deletePostByID(Integer postID);
}
