package com.trendythread.app.controllers;

import com.trendythread.app.constants.AppConstants;
import com.trendythread.app.payloads.ApiResponse;
import com.trendythread.app.dto.PostDto;
import com.trendythread.app.dto.PostViewCountDto;
import com.trendythread.app.dto.TrendingPostDto;
import com.trendythread.app.payloads.PostResponse;
import com.trendythread.app.services.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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

import jakarta.validation.Valid;

import java.security.Principal;
import java.util.List;

@Tag(
        name = "CRUD REST APIs for POST in TrendyThread",
        description = "CRUD REST APIs in TrendyThread to CREATE, UPDATE, FETCH and DELETE post details"
)
@RestController
@RequestMapping("/api/v1/posts")
@Slf4j
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
    @PostMapping("/category/{categoryID}")
    public ResponseEntity<PostDto> createPost(@Valid @RequestBody PostDto postDto, @PathVariable Integer categoryID, Principal  principal) {
        String authenticatedUserEmail = principal.getName(); // Gets the email of logged-in user
        log.info("POST /api/v1/posts/category/{} - createPost request received: postDto={}", categoryID, postDto);
        PostDto newPost = this.postService.createPost(postDto, authenticatedUserEmail, categoryID);
        log.info("POST /api/v1/posts - created new post for blogger={}, category={} -> {}", authenticatedUserEmail, categoryID, newPost);

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
        log.info("GET /api/v1/posts/{} - fetchByPostId request received", postId);
        PostDto postDto = this.postService.findByPostId(postId);
        log.debug("GET /api/v1/posts/{} - fetched post: {}", postId, postDto);
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
        log.info("GET /api/v1/posts - fetchAllPosts request received: pageNumber={}, pageSize={}, sortBy={}, isAsc={}", pageNumber, pageSize, sortBy, isAsc);
        PostResponse postResponse = this.postService.findAllPosts(pageNumber, pageSize, sortBy, isAsc);
        log.debug("GET /api/v1/posts - fetched posts response: {}", postResponse);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    // GET Mapping - Get Personalized Feed
    @Operation(
            summary = "FETCH Personalized Feed REST API",
            description = "REST API to fetch posts from bloggers the authenticated user follows"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping("/feed")
    public ResponseEntity<PostResponse> getFeed(
            @RequestParam(value = "pageNumber", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR, required = false) boolean isAsc,
            Principal principal) {
        log.info("GET /api/v1/posts/feed - getFeed request received: user={}", principal.getName());
        PostResponse feed = this.postService.getFeed(principal.getName(), pageNumber, pageSize, sortBy, isAsc);
        return new ResponseEntity<>(feed, HttpStatus.OK);
    }

    // GET Mapping - Get Trending Posts
    @Operation(
            summary = "FETCH Trending Posts REST API",
            description = "REST API to fetch the most-viewed posts, ordered by view count descending"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping("/trending")
    public ResponseEntity<List<TrendingPostDto>> getTrendingPosts(
            @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit) {
        log.info("GET /api/v1/posts/trending - getTrendingPosts request received: limit={}", limit);
        List<TrendingPostDto> trending = this.postService.getTrendingPosts(limit);
        log.debug("GET /api/v1/posts/trending - returning {} trending posts", trending.size());
        return new ResponseEntity<>(trending, HttpStatus.OK);
    }

    // GET Mapping - Get view count for a post
    @Operation(
            summary = "FETCH Post View Count REST API",
            description = "REST API to fetch the total view count for a post"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping("/{postId}/views")
    public ResponseEntity<PostViewCountDto> getViewCount(@PathVariable Integer postId) {
        log.info("GET /api/v1/posts/{}/views - getViewCount request received", postId);
        return new ResponseEntity<>(this.postService.getViewCount(postId), HttpStatus.OK);
    }

    // GET Mapping - Get Latest Posts
    @Operation(
            summary = "FETCH Latest Posts REST API",
            description = "REST API to fetch latest posts in TrendyThread"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping("/latest")
    public ResponseEntity<PostResponse> getLatestPosts(
            @RequestParam(value = "limit", defaultValue = "6", required = false) Integer limit) {
        log.info("GET /api/v1/posts/latest - getLatestPosts request received: limit={}", limit);
        PostResponse postResponse = this.postService.getLatestPosts(limit);
        log.debug("GET /api/v1/posts/latest - fetched {} latest posts", postResponse.getContent().size());
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
        log.info("GET /api/v1/posts/category/{} - fetchByCategoryId request received: pageNumber={}, pageSize={}, sortBy={}, isAsc={}", categoryId, pageNumber, pageSize, sortBy, isAsc);
        PostResponse postResponse = this.postService.findPostsByCategoryId(categoryId, pageNumber, pageSize, sortBy, isAsc);
        log.debug("GET /api/v1/posts/category/{} - fetched posts response: {}", categoryId, postResponse);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    // GET Mapping - Get by Tag
    @Operation(
            summary = "FETCH Post REST API",
            description = "REST API to fetch all Posts tagged with a given tag name"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping("/tag/{tagName}")
    public ResponseEntity<PostResponse> fetchByTag(@PathVariable String tagName,
                                                    @RequestParam(value = "pageNumber", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) Integer pageNumber,
                                                    @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) Integer pageSize,
                                                    @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
                                                    @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR, required = false) boolean isAsc) {
        log.info("GET /api/v1/posts/tag/{} - fetchByTag request received: pageNumber={}, pageSize={}, sortBy={}, isAsc={}", tagName, pageNumber, pageSize, sortBy, isAsc);
        PostResponse postResponse = this.postService.findPostsByTag(tagName, pageNumber, pageSize, sortBy, isAsc);
        log.debug("GET /api/v1/posts/tag/{} - fetched posts response: {}", tagName, postResponse);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    // GET Mapping - Get by Blogger
    @Operation(
            summary = "FETCH Post REST API",
            description = "REST API to fetch all Posts based on blogger-id"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping("/blogger")
    public ResponseEntity<PostResponse> fetchByBloggerId(@RequestParam(value = "pageNumber", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) Integer pageNumber,
                                                      @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) Integer pageSize,
                                                      @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
                                                      @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR, required = false) boolean isAsc,
                                                        Principal principal) {
        String authenticatedUserEmail = principal.getName(); // Gets the email of logged-in user
        log.info("GET /api/v1/posts/blogger - fetchByBloggerId request received: email={}, pageNumber={}, pageSize={}, sortBy={}, isAsc={}", authenticatedUserEmail, pageNumber, pageSize, sortBy, isAsc);
        PostResponse postResponse = this.postService.findPostsByBloggerId(authenticatedUserEmail, pageNumber, pageSize, sortBy, isAsc);
        log.debug("GET /api/v1/posts/blogger email={} - fetched posts response: {}", authenticatedUserEmail, postResponse);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    // GET Mapping - Get by User ID
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
        log.info("GET /api/v1/posts/user/{} - fetchByUserId request received: pageNumber={}, pageSize={}, sortBy={}, isAsc={}", userId, pageNumber, pageSize, sortBy, isAsc);
        PostResponse postResponse = this.postService.findPostsByUserId(userId, pageNumber, pageSize, sortBy, isAsc);
        log.debug("GET /api/v1/posts/user/{} - fetched posts response: {}", userId, postResponse);
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
    public ResponseEntity<PostDto> updateByPostId(@PathVariable Integer postId, @Valid @RequestBody PostDto postDto, Principal principal) {
        String authenticatedUserEmail = principal.getName(); // Gets the email of logged-in user
        log.info("PUT /api/v1/posts/{} - updateByPostId request received: postDto={}, authenticatedUser={}", postId, postDto, authenticatedUserEmail);
        PostDto savedPostDto = this.postService.updateByPostId(postId, postDto, authenticatedUserEmail);
        log.info("PUT /api/v1/posts/{} - update successful: {}", postId, savedPostDto);
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
        log.info("DELETE /api/v1/posts/{} - deleteByPostId request received", postId);
        this.postService.deleteByPostId(postId);
        log.info("DELETE /api/v1/posts/{} - deletion completed", postId);
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
        log.info("GET /api/v1/posts/search/{} - searchPosts request received: pageNumber={}, pageSize={}, sortBy={}, isAsc={}", keyword, pageNumber, pageSize, sortBy, isAsc);
        PostResponse postResponse = this.postService.searchPost(keyword, pageNumber, pageSize, sortBy, isAsc);
        log.debug("GET /api/v1/posts/search/{} - search response: {}", keyword, postResponse);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

}
