package com.blog.app.controllers;

import com.blog.app.config.AppConstants;
import com.blog.app.payloads.ApiResponse;
import com.blog.app.payloads.PostDto;
import com.blog.app.payloads.PostResponse;
import com.blog.app.services.FileService;
import com.blog.app.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;

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
    public ResponseEntity<PostResponse> getAllPost(
            @RequestParam(value = "pageNumber", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR, required = false) boolean isAsc) {
        PostResponse postResponse = this.postService.getAllPosts(pageNumber, pageSize, sortBy, isAsc);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    // GET Mapping - Get by Category
    @GetMapping("/category/{categoryID}")
    public ResponseEntity<PostResponse> getPostsByCategory(@PathVariable Integer categoryID,
                                                           @RequestParam(value = "pageNumber", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) Integer pageNumber,
                                                           @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) Integer pageSize,
                                                           @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
                                                           @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR, required = false) boolean isAsc) {
        PostResponse postResponse = this.postService.getAllPostsByCategory(categoryID, pageNumber, pageSize, sortBy, isAsc);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    // GET Mapping - Get by User
    @GetMapping("/user/{userID}")
    public ResponseEntity<PostResponse> getPostsByUser(@PathVariable Integer userID,
                                                       @RequestParam(value = "pageNumber", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) Integer pageNumber,
                                                       @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) Integer pageSize,
                                                       @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
                                                       @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR, required = false) boolean isAsc) {
        PostResponse postResponse = this.postService.getAllPostsByUser(userID, pageNumber, pageSize, sortBy, isAsc);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
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

    // GET Mapping for Search
    @GetMapping("/search/{keyword}")
    public ResponseEntity<PostResponse> searchPosts(@PathVariable String keyword,
                                                    @RequestParam(value = "pageNumber", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) Integer pageNumber,
                                                    @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) Integer pageSize,
                                                    @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
                                                    @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR, required = false) boolean isAsc) {
        PostResponse postResponse = this.postService.searchPost(keyword, pageNumber, pageSize, sortBy, isAsc);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    // Post image upload
    @PostMapping("/image/upload/{postId}")
    public ResponseEntity<PostDto> uploadPostImage(
            @RequestParam("image") MultipartFile image,
            @PathVariable Integer postId) throws IOException {

        PostDto postDto = this.postService.getPostByID(postId);
        String fileName = this.fileService.uploadImage(path, image);
        postDto.setImageName(fileName);

        PostDto updatedPostDto = this.postService.updatePostById(postId, postDto);
        return new ResponseEntity<>(updatedPostDto, HttpStatus.OK);
    }

}
