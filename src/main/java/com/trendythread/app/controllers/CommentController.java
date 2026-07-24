package com.trendythread.app.controllers;

import com.trendythread.app.payloads.ApiResponse;
import com.trendythread.app.dto.CommentDto;
import com.trendythread.app.services.CommentService;
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
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.security.Principal;
import java.util.List;

@Tag(
        name = "CRUD REST APIs for COMMENT in TrendyThread",
        description = "CRUD REST APIs in TrendyThread to CREATE, UPDATE, FETCH and DELETE comments"
)
@RestController
@RequestMapping("/api/v1/")
@Slf4j
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Operation(
            summary = "CREATE Comment REST API",
            description = "REST API to create a new comment based on post-id"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "HTTP Status CREATED"
    )
    @PostMapping("/post/{postId}/comments")
    public ResponseEntity<CommentDto> createComment(@PathVariable Integer postId, @RequestBody CommentDto commentDto, Principal principal) {
        String authenticatedUserEmail = principal.getName(); // Gets the email of logged-in user
        log.info("POST /api/v1/post/{}/comments - createComment request received: email={}, commentDto={}", postId, authenticatedUserEmail, commentDto);
        CommentDto savedCommentDto = this.commentService.createComment(commentDto, postId, authenticatedUserEmail);
        log.info("POST /api/v1/post/{}/comments - comment created: {}", postId, savedCommentDto);

        return new ResponseEntity<>(savedCommentDto, HttpStatus.CREATED);
    }

    @Operation(
            summary = "FETCH Comment REST API",
            description = "REST API to fetch comments based on post-id"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping("/posts/{postId}/comments")
    public List<CommentDto> findByPostId(@PathVariable(value = "postId") Integer postId){
        log.info("GET /api/v1/posts/{}/comments - request received", postId);
        List<CommentDto> results = commentService.findByPostId(postId);
        log.debug("GET /api/v1/posts/{}/comments - found {} comments", postId, results == null ? 0 : results.size());
        return results;
    }

    @Operation(
            summary = "FETCH Comment REST API",
            description = "REST API to fetch a comment based on post-id and comment-id"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping("/posts/{postId}/comments/{id}")
    public ResponseEntity<CommentDto> fetchByPostIdAndCommentId(@PathVariable(value = "postId") Integer postId,
                                                                @PathVariable(value = "id") Integer commentId){
        log.info("GET /api/v1/posts/{}/comments/{} - request received", postId, commentId);
        CommentDto commentDto = commentService.findByPostIdAndCommentId(postId, commentId);
        log.debug("GET /api/v1/posts/{}/comments/{} - fetched comment: {}", postId, commentId, commentDto);
        return new ResponseEntity<>(commentDto, HttpStatus.OK);
    }

    @Operation(
            summary = "UPDATE Comment REST API",
            description = "REST API to update a comment based on post-id and comment-id"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @PutMapping("/posts/{postId}/comments/{id}")
    public ResponseEntity<CommentDto> updateByPostIdAndCommentId(@PathVariable(value = "postId") Integer postId,
                                                                 @PathVariable(value = "id") Integer commentId,
                                                                 @Valid @RequestBody CommentDto commentDto,
                                                                 Principal principal){
        String authenticatedUserEmail = principal.getName(); // Gets the email of logged-in user
        log.info("PUT /api/v1/posts/{}/comments/{} - update request: email={}, {}", postId, commentId, authenticatedUserEmail, commentDto);
        CommentDto updatedComment = commentService.updateByPostIdAndCommentId(postId, commentId, commentDto, authenticatedUserEmail);
        log.info("PUT /api/v1/posts/{}/comments/{} - update successful: {}", postId, commentId, updatedComment);
        return new ResponseEntity<>(updatedComment, HttpStatus.OK);
    }

    @Operation(
            summary = "DELETE Comment REST API",
            description = "REST API to delete comment based on comment-id"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse> deleteComment(@PathVariable Integer commentId) {
        log.info("DELETE /api/v1/posts/comments/{} - delete request received", commentId);
        this.commentService.deleteByCommentId(commentId);
        log.info("DELETE /api/v1/posts/comments/{} - deletion completed", commentId);

        return new ResponseEntity<>(new ApiResponse("Comment Deleted Successfully.", true), HttpStatus.CREATED);
    }
}
