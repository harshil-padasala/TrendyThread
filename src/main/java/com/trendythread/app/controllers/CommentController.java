package com.trendythread.app.controllers;

import com.trendythread.app.payloads.ApiResponse;
import com.trendythread.app.dto.CommentDto;
import com.trendythread.app.services.CommentService;
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
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Tag(
        name = "CRUD REST APIs for COMMENT in TrendyThread",
        description = "CRUD REST APIs in TrendyThread to CREATE, UPDATE, FETCH and DELETE comments"
)
@RestController
@RequestMapping("/api/v1/")
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
    public ResponseEntity<CommentDto> createComment(@PathVariable Integer postId, @RequestBody CommentDto commentDto) {
        CommentDto commentDto1 = this.commentService.createComment(commentDto, postId);

        return new ResponseEntity<>(commentDto1, HttpStatus.CREATED);
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
        return commentService.findByPostId(postId);
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
        CommentDto commentDto = commentService.findByPostIdAndCommentId(postId, commentId);
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
                                                                 @Valid @RequestBody CommentDto commentDto){
        CommentDto updatedComment = commentService.updateByPostIdAndCommentId(postId, commentId, commentDto);
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
        this.commentService.deleteByCommentId(commentId);

        return new ResponseEntity<>(new ApiResponse("Comment Deleted Successfully.", true), HttpStatus.CREATED);
    }
}
