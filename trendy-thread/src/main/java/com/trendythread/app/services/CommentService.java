package com.trendythread.app.services;

import com.trendythread.app.dto.CommentDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CommentService {

    CommentDto createComment(CommentDto commentDto, Integer postId, String authenticatedUserEmail);

    List<CommentDto> findByPostId(Integer postId);

    CommentDto findByPostIdAndCommentId(Integer postId, Integer commentId);

    CommentDto updateByPostIdAndCommentId(Integer postId, Integer commentId, CommentDto commentRequest, String authenticatedUserEmail);

    void deleteByCommentId(Integer commentId);
}
