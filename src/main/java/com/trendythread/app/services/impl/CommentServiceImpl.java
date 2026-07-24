package com.trendythread.app.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trendythread.app.constants.IRedisConstant;
import com.trendythread.app.constants.IRedisTtlConstant;
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
import com.trendythread.app.util.RedisCacheEvictionHelper;
import com.trendythread.app.util.RedisCacheSupport;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final PostRepository postRepository;

    private final BloggersRepository bloggersRepository;

    private final ModelMapper modelMapper;

    private final RedisCacheSupport redisCacheSupport;

    private final RedisCacheEvictionHelper redisCacheEvictionHelper;

    private final ObjectMapper objectMapper;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,
                              PostRepository postRepository,
                              BloggersRepository bloggersRepository,
                              ModelMapper modelMapper,
                              RedisCacheSupport redisCacheSupport,
                              RedisCacheEvictionHelper redisCacheEvictionHelper,
                              ObjectMapper objectMapper) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.bloggersRepository = bloggersRepository;
        this.modelMapper = modelMapper;
        this.redisCacheSupport = redisCacheSupport;
        this.redisCacheEvictionHelper = redisCacheEvictionHelper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
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

        scheduleCommentCacheRefreshAfterCommit(postId, Set.of(savedComment.getId()));
        scheduleAfterCommit(() -> {
            redisCacheEvictionHelper.evictPostCollectionCaches();
            redisCacheEvictionHelper.evictPostIdCaches(Set.of(postId));
        });

        log.info("createComment - comment created: id={}", result.getId());
        return result;
    }

    @Override
    public List<CommentDto> findByPostId(Integer postId) {
        log.info("findByPostId - request received: postId={}", postId);

        String cacheKey = buildCommentListCacheKey(postId);
        Object cachedComments = redisCacheSupport.get(cacheKey);
        if (cachedComments != null) {
            log.info("findByPostId - cache hit for postId={}", postId);
            return objectMapper.convertValue(cachedComments, new TypeReference<>() {});
        }

        List<Comment> comments = commentRepository.findByPostId(postId);

        List<CommentDto> result = comments.stream().map(comment -> {
            return toCommentDto(comment);
        }).collect(Collectors.toList());

        redisCacheSupport.set(cacheKey, result, IRedisTtlConstant.TTL_QUERY);

        log.debug("findByPostId - found {} comments for postId={}", result.size(), postId);
        return result;

    }

    @Override
    public CommentDto findByPostIdAndCommentId(Integer postId, Integer commentId) {
        log.info("findByPostIdAndCommentId - request received: postId={}, commentId={}", postId, commentId);

        String cacheKey = buildCommentDetailCacheKey(postId, commentId);
        Object cachedComment = redisCacheSupport.get(cacheKey);
        if (cachedComment != null) {
            log.info("findByPostIdAndCommentId - cache hit for postId={}, commentId={}", postId, commentId);
            return objectMapper.convertValue(cachedComment, CommentDto.class);
        }

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

        CommentDto result = toCommentDto(comment);
        redisCacheSupport.set(cacheKey, result, IRedisTtlConstant.TTL_ENTITY);
        log.debug("findByPostIdAndCommentId - fetched comment: {}", result);
        return result;
    }

    @Override
    @Transactional
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

        CommentDto result = toCommentDto(updatedComment);
        scheduleCommentCacheRefreshAfterCommit(postId, Set.of(updatedComment.getId()));
        scheduleAfterCommit(() -> {
            redisCacheEvictionHelper.evictPostCollectionCaches();
            redisCacheEvictionHelper.evictPostIdCaches(Set.of(postId));
        });
        log.info("updateByPostIdAndCommentId - update successful: id={}", result.getId());
        return result;
    }

    @Override
    @Transactional
    public void deleteByCommentId(Integer commentId) {
        log.info("deleteByCommentId - request received: id={}", commentId);
        Comment comment = this.commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "Comment Id", commentId));
        Integer postId = comment.getPost().getId();

        this.commentRepository.delete(comment);

        scheduleCommentCacheRefreshAfterCommit(postId, Set.of());
        scheduleAfterCommit(() -> {
            redisCacheEvictionHelper.evictPostCollectionCaches();
            redisCacheEvictionHelper.evictPostIdCaches(Set.of(postId));
        });

        log.info("deleteByCommentId - deleted comment id={}", commentId);

    }

    private CommentDto toCommentDto(Comment comment) {
        CommentDto dto = this.modelMapper.map(comment, CommentDto.class);
        if (comment.getBlogger() != null) {
            dto.setName(comment.getBlogger().getFirstName() + " " + comment.getBlogger().getLastName());
            dto.setEmail(comment.getBlogger().getEmail());
        }
        return dto;
    }

    private void scheduleCommentCacheRefreshAfterCommit(Integer postId, Set<Integer> commentIdsToRefresh) {
        scheduleAfterCommit(() -> {
            redisCacheEvictionHelper.evictCommentCachesForPost(postId);
            refreshCommentDetailCaches(postId, commentIdsToRefresh);
        });
    }

    private void scheduleAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
            return;
        }

        task.run();
    }

    private void refreshCommentDetailCaches(Integer postId, Set<Integer> commentIdsToRefresh) {
        if (commentIdsToRefresh == null || commentIdsToRefresh.isEmpty()) {
            return;
        }

        for (Integer commentId : commentIdsToRefresh) {
            commentRepository.findById(commentId).ifPresent(comment ->
                    redisCacheSupport.set(buildCommentDetailCacheKey(postId, commentId), toCommentDto(comment), IRedisTtlConstant.TTL_ENTITY)
            );
        }

        log.debug("refreshCommentDetailCaches - refreshed {} comment detail cache entries for postId={}", commentIdsToRefresh.size(), postId);
    }

    private String buildCommentListCacheKey(Integer postId) {
        return IRedisConstant.REDIS_COMMENT_POST.concat(String.valueOf(postId)).concat(IRedisConstant.REDIS_COMMENT_LIST_SUFFIX);
    }

    private String buildCommentDetailCacheKey(Integer postId, Integer commentId) {
        return IRedisConstant.REDIS_COMMENT_POST
                .concat(String.valueOf(postId))
                .concat(IRedisConstant.REDIS_COMMENT_ID_SUFFIX)
                .concat(String.valueOf(commentId));
    }
}
