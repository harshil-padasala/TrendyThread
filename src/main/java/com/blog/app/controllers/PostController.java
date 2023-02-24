package com.blog.app.controllers;

import com.blog.app.payloads.ApiResponse;
import com.blog.app.payloads.PostDto;
import com.blog.app.services.PostService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    // POST Mapping - Create Post
    @PostMapping("/users/{userID}/category/{categoryID}")
    public ResponseEntity<PostDto> createPost(@Valid @RequestBody PostDto postDto, @PathVariable Integer userID, @PathVariable Integer categoryID) {
        PostDto newPost = this.postService.createPost(postDto, userID, categoryID);

        return new ResponseEntity<>(newPost, HttpStatus.CREATED);
    }

    // GET Mapping - Get By PostID
    @GetMapping("/{postID}")
    public ResponseEntity<PostDto> getPostById(@PathVariable Integer postID) {
        PostDto postDto = this.postService.getPostByID(postID);
        return new ResponseEntity<>(postDto, HttpStatus.OK);
    }

    // GET Mapping - Get All
    @GetMapping
    public ResponseEntity<List<PostDto>> getAllPost() {
        List<PostDto> postDtoList = this.postService.getAllPosts();
        return new ResponseEntity<>(postDtoList, HttpStatus.OK);
    }

    // GET Mapping - Get by Category
    @GetMapping("/category/{categoryID}")
    public ResponseEntity<List<PostDto>> getPostsByCategory(@PathVariable Integer categoryID) {
        List<PostDto> postDtoList = this.postService.getAllPostsByCategory(categoryID);
        return new ResponseEntity<>(postDtoList, HttpStatus.OK);
    }

    // GET Mapping - Get by User
    @GetMapping("/user/{userID}")
    public ResponseEntity<List<PostDto>> getPostsByUser(@PathVariable Integer userID) {
        List<PostDto> postDtoList = this.postService.getAllPostsByUser(userID);
        return new ResponseEntity<>(postDtoList, HttpStatus.OK);
    }

    // PUT Mapping By ID
    @PutMapping("/{postID}")
    public ResponseEntity<PostDto> updatePostByID(@PathVariable Integer postID, @Valid @RequestBody PostDto postDto) {
        PostDto savedPostDto = this.postService.updatePostById(postID, postDto);
        return new ResponseEntity<>(savedPostDto, HttpStatus.OK);
    }

    // PUT Mapping By ID
    @DeleteMapping("/{postID}")
    public ResponseEntity<ApiResponse> deletePostByID(@PathVariable Integer postID) {
        this.postService.deletePostByID(postID);
        return new ResponseEntity<>(
                new ApiResponse("Post has been deleted!!", true),
                HttpStatus.OK);
    }

}
