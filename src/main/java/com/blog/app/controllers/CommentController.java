package com.blog.app.controllers;

import com.blog.app.entities.Comment;
import com.blog.app.payloads.ApiResponse;
import com.blog.app.payloads.CommentDto;
import com.blog.app.services.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/post/{postId}")
    public ResponseEntity<CommentDto> createComment(@PathVariable Integer postId, @RequestBody CommentDto commentDto) {
        CommentDto commentDto1 = this.commentService.createComment(commentDto, postId);

        return new ResponseEntity<>(commentDto1, HttpStatus.CREATED);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse> deleteComment(@PathVariable Integer commentId) {
        this.commentService.deleteComment(commentId);

        return new ResponseEntity<>(new ApiResponse("Comment Deleted Successfully.", true), HttpStatus.CREATED);
    }
}
