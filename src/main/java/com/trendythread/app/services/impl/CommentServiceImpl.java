package com.trendythread.app.services.impl;

import com.trendythread.app.entities.Comment;
import com.trendythread.app.entities.Post;
import com.trendythread.app.exceptions.BlogAPIException;
import com.trendythread.app.exceptions.ResourceNotFoundException;
import com.trendythread.app.dto.CommentDto;
import com.trendythread.app.repositories.CommentRepository;
import com.trendythread.app.repositories.PostRepository;
import com.trendythread.app.services.CommentService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CommentDto createComment(CommentDto commentDto, Integer postId) {
        Post post = this.postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postId));

        Comment comment = this.modelMapper.map(commentDto, Comment.class);
        comment.setPost(post);

        Comment savedComment = this.commentRepository.save(comment);
        return this.modelMapper.map(savedComment, CommentDto.class);
    }

    @Override
    public List<CommentDto> findByPostId(Integer postId) {

        List<Comment> comments = commentRepository.findByPostId(postId);

        return comments.stream().map(comment -> this.modelMapper.map(comment, CommentDto.class)).collect(Collectors.toList());

    }

    @Override
    public CommentDto findByPostIdAndCommentId(Integer postId, Integer commentId) {

        // retrieve post entity by id
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new ResourceNotFoundException("Post", "id", postId));

        // retrieve comment by id
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new ResourceNotFoundException("Comment", "id", commentId));

        if(!comment.getPost().getId().equals(post.getId())){
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Comment does not belong to post");
        }

        return this.modelMapper.map(comment, CommentDto.class);
    }

    @Override
    public CommentDto updateByPostIdAndCommentId(Integer postId, Integer commentId, CommentDto commentRequest) {

        // retrieve post entity by id
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new ResourceNotFoundException("Post", "id", postId));

        // retrieve comment by id
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new ResourceNotFoundException("Comment", "id", commentId));

        if(!comment.getPost().getId().equals(post.getId())){
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Comment does not belong to post");
        }

        comment.setName(commentRequest.getName());
        comment.setEmail(commentRequest.getEmail());
        comment.setContent(commentRequest.getContent());

        Comment updatedComment = commentRepository.save(comment);

        return this.modelMapper.map(updatedComment, CommentDto.class);
    }

    @Override
    public void deleteByCommentId(Integer commentId) {
        this.commentRepository.delete(this.commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "Comment Id", commentId)));


    }
}
