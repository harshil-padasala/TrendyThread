package com.trendythread.app.services.impl;

import com.trendythread.app.entities.Blogger;
import com.trendythread.app.entities.Comment;
import com.trendythread.app.entities.Post;
import com.trendythread.app.exceptions.BlogAPIException;
import com.trendythread.app.exceptions.ResourceNotFoundException;
import com.trendythread.app.dto.CommentDto;
import com.trendythread.app.repositories.BloggersRepository;
import com.trendythread.app.repositories.CommentRepository;
import com.trendythread.app.repositories.PostRepository;
import com.trendythread.app.services.CommentService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final PostRepository postRepository;

    private final BloggersRepository bloggersRepository;

    private final ModelMapper modelMapper;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,
                              PostRepository postRepository,
                              BloggersRepository bloggersRepository,
                              ModelMapper modelMapper) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.bloggersRepository = bloggersRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public CommentDto createComment(CommentDto commentDto, Integer postId, String authenticatedUserEmail) {
        log.info("createComment - request received: postId={}, commentDto={}", postId, commentDto);
        Post post = this.postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postId));

        Blogger blogger = bloggersRepository.findByEmail(authenticatedUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Blogger", "email", authenticatedUserEmail));

        Comment comment = this.modelMapper.map(commentDto, Comment.class);
        comment.setPost(post);
        comment.setBlogger(blogger);

        Comment savedComment = this.commentRepository.save(comment);
        CommentDto result = this.modelMapper.map(savedComment, CommentDto.class);
        // Set name and email from blogger entity
        if (savedComment.getBlogger() != null) {
            result.setName(savedComment.getBlogger().getFirstName() + " " + savedComment.getBlogger().getLastName());
            result.setEmail(savedComment.getBlogger().getEmail());
        }
        log.info("createComment - comment created: id={}", result.getId());
        return result;
    }

    @Override
    public List<CommentDto> findByPostId(Integer postId) {
        log.info("findByPostId - request received: postId={}", postId);

        List<Comment> comments = commentRepository.findByPostId(postId);

        List<CommentDto> result = comments.stream().map(comment -> {
            CommentDto dto = this.modelMapper.map(comment, CommentDto.class);
            // Set name and email from blogger entity
            if (comment.getBlogger() != null) {
                dto.setName(comment.getBlogger().getFirstName() + " " + comment.getBlogger().getLastName());
                dto.setEmail(comment.getBlogger().getEmail());
            }
            return dto;
        }).collect(Collectors.toList());

        log.debug("findByPostId - found {} comments for postId={}", result.size(), postId);
        return result;

    }

    @Override
    public CommentDto findByPostIdAndCommentId(Integer postId, Integer commentId) {
        log.info("findByPostIdAndCommentId - request received: postId={}, commentId={}", postId, commentId);

        // retrieve post entity by id
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new ResourceNotFoundException("Post", "id", postId));

        // retrieve comment by id
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new ResourceNotFoundException("Comment", "id", commentId));

        if(!comment.getPost().getId().equals(post.getId())){
            log.warn("findByPostIdAndCommentId - comment {} does not belong to post {}", commentId, postId);
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Comment does not belong to post");
        }

        CommentDto result = this.modelMapper.map(comment, CommentDto.class);
        // Set name and email from blogger entity
        if (comment.getBlogger() != null) {
            result.setName(comment.getBlogger().getFirstName() + " " + comment.getBlogger().getLastName());
            result.setEmail(comment.getBlogger().getEmail());
        }
        log.debug("findByPostIdAndCommentId - fetched comment: {}", result);
        return result;
    }

    @Override
    public CommentDto updateByPostIdAndCommentId(Integer postId, Integer commentId, CommentDto commentRequest, String authenticatedUserEmail) {
        log.info("updateByPostIdAndCommentId - request received: postId={}, commentId={}, email={}, commentRequest={}", postId, commentId, authenticatedUserEmail, commentRequest);

        // retrieve post entity by id
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new ResourceNotFoundException("Post", "id", postId));

        // retrieve comment by id
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new ResourceNotFoundException("Comment", "id", commentId));

        if(!comment.getPost().getId().equals(post.getId())){
            log.warn("updateByPostIdAndCommentId - comment {} does not belong to post {}", commentId, postId);
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Comment does not belong to post");
        }

        // Validate that the authenticated user is the comment creator
        if (comment.getBlogger() == null || !comment.getBlogger().getEmail().equals(authenticatedUserEmail)) {
            log.warn("updateByPostIdAndCommentId - user {} is not the creator of comment {}", authenticatedUserEmail, commentId);
            throw new BlogAPIException(HttpStatus.FORBIDDEN, "You can only update your own comments");
        }

        comment.setContent(commentRequest.getContent());

        Comment updatedComment = commentRepository.save(comment);

        CommentDto result = this.modelMapper.map(updatedComment, CommentDto.class);
        // Set name and email from blogger entity
        if (updatedComment.getBlogger() != null) {
            result.setName(updatedComment.getBlogger().getFirstName() + " " + updatedComment.getBlogger().getLastName());
            result.setEmail(updatedComment.getBlogger().getEmail());
        }
        log.info("updateByPostIdAndCommentId - update successful: id={}", result.getId());
        return result;
    }

    @Override
    public void deleteByCommentId(Integer commentId) {
        log.info("deleteByCommentId - request received: id={}", commentId);
        this.commentRepository.delete(this.commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "Comment Id", commentId)));
        log.info("deleteByCommentId - deleted comment id={}", commentId);

    }
}
