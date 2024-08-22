package com.trendythread.app.controllers;

import com.trendythread.app.config.AppConstants;
import com.trendythread.app.payloads.ApiResponse;
import com.trendythread.app.dto.PostDto;
import com.trendythread.app.payloads.PostResponse;
import com.trendythread.app.services.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(
        name = "CRUD REST APIs for POST in TrendyThread",
        description = "CRUD REST APIs in TrendyThread to CREATE, UPDATE, FETCH and DELETE post details"
)
@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    @Autowired
    private PostService postService;

    // POST Mapping - Create Post
    @Operation(
            summary = "CREATE Post REST API",
            description = "REST API to create a new POST in TrendyThread"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "HTTP Status Created"
    )
    @PostMapping("/users/{userID}/category/{categoryID}")
    public ResponseEntity<PostDto> createPost(@Valid @RequestBody PostDto postDto, @PathVariable Integer userID, @PathVariable Integer categoryID) {
        PostDto newPost = this.postService.createPost(postDto, userID, categoryID);

        return new ResponseEntity<>(newPost, HttpStatus.CREATED);
    }

    // GET Mapping - Get By PostID
    @Operation(
            summary = "FETCH Post REST API",
            description = "REST API to fetch a Post details based on post-id"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> fetchByPostId(@PathVariable Integer postId) {
        PostDto postDto = this.postService.findByPostId(postId);
        return new ResponseEntity<>(postDto, HttpStatus.OK);
    }

    // GET Mapping - Get All
    @Operation(
            summary = "FETCH Post REST API",
            description = "REST API to fetch all Post in TrendyThread"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping
    public ResponseEntity<PostResponse> fetchAllPosts(
            @RequestParam(value = "pageNumber", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR, required = false) boolean isAsc) {
        PostResponse postResponse = this.postService.findAllPosts(pageNumber, pageSize, sortBy, isAsc);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    // GET Mapping - Get by Category
    @Operation(
            summary = "FETCH Post REST API",
            description = "REST API to fetch all Posts based on category-id"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<PostResponse> fetchByCategoryId(@PathVariable Integer categoryId,
                                                          @RequestParam(value = "pageNumber", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) Integer pageNumber,
                                                          @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) Integer pageSize,
                                                          @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
                                                          @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR, required = false) boolean isAsc) {
        PostResponse postResponse = this.postService.findPostsByCategoryId(categoryId, pageNumber, pageSize, sortBy, isAsc);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    // GET Mapping - Get by User
    @Operation(
            summary = "FETCH Post REST API",
            description = "REST API to fetch all Posts based on user-id"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<PostResponse> fetchByUserId(@PathVariable Integer userId,
                                                      @RequestParam(value = "pageNumber", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) Integer pageNumber,
                                                      @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) Integer pageSize,
                                                      @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
                                                      @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR, required = false) boolean isAsc) {
        PostResponse postResponse = this.postService.findPostsByUserId(userId, pageNumber, pageSize, sortBy, isAsc);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    // PUT Mapping By ID
    @Operation(
            summary = "UPDATE Post Details REST API",
            description = "REST API to update a Post based on post-id"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @PutMapping("/{postId}")
    public ResponseEntity<PostDto> updateByPostId(@PathVariable Integer postId, @Valid @RequestBody PostDto postDto) {
        PostDto savedPostDto = this.postService.updateByPostId(postId, postDto);
        return new ResponseEntity<>(savedPostDto, HttpStatus.OK);
    }

    // PUT Mapping By ID
    @Operation(
            summary = "DELETE Post Details REST API",
            description = "REST API to DELETE a Post based on post-id"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse> deleteByPostId(@PathVariable Integer postId) {
        this.postService.deleteByPostId(postId);
        return new ResponseEntity<>(
                new ApiResponse("Post has been deleted!!", true),
                HttpStatus.OK);
    }

    // GET Mapping for Search
    @Operation(
            summary = "FETCH Post REST API",
            description = "REST API to fetch Posts based on keyword"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping("/search/{keyword}")
    public ResponseEntity<PostResponse> searchPosts(@PathVariable String keyword,
                                                    @RequestParam(value = "pageNumber", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) Integer pageNumber,
                                                    @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) Integer pageSize,
                                                    @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
                                                    @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR, required = false) boolean isAsc) {
        PostResponse postResponse = this.postService.searchPost(keyword, pageNumber, pageSize, sortBy, isAsc);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

}
