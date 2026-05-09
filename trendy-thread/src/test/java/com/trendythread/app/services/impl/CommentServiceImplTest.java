package com.trendythread.app.services.impl;

import com.trendythread.app.constants.IRedisConstant;
import com.trendythread.app.dto.CommentDto;
import com.trendythread.app.entities.Blogger;
import com.trendythread.app.entities.Comment;
import com.trendythread.app.entities.Post;
import com.trendythread.app.repositories.BloggersRepository;
import com.trendythread.app.repositories.CommentRepository;
import com.trendythread.app.repositories.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private BloggersRepository bloggersRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void findByPostIdReturnsCachedCommentsWithoutRepositoryLookup() {
        CommentServiceImpl service = new CommentServiceImpl(commentRepository, postRepository, bloggersRepository, modelMapper, redisTemplate);
        CommentDto cachedComment = commentDto(4, "Cached comment");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(IRedisConstant.REDIS_COMMENT_POST.concat("12:LIST"))).thenReturn(List.of(cachedComment));

        List<CommentDto> result = service.findByPostId(12);

        assertEquals(1, result.size());
        assertEquals(4, result.get(0).getId());
        verify(commentRepository, never()).findByPostId(12);
    }

    @Test
    void createCommentRefreshesCommentCachesAndEvictsRelatedPostCachesAfterCommit() {
        CommentServiceImpl service = new CommentServiceImpl(commentRepository, postRepository, bloggersRepository, modelMapper, redisTemplate);
        Post post = post(14);
        Blogger blogger = blogger("reader@example.com");
        CommentDto request = commentDto(null, "A thoughtful comment body");
        Comment newComment = comment(9, "A thoughtful comment body", post, blogger);
        Comment savedComment = comment(9, "A thoughtful comment body", post, blogger);
        CommentDto savedDto = commentDto(9, "A thoughtful comment body");
        Set<String> commentKeys = Set.of(IRedisConstant.REDIS_COMMENT_POST.concat("14:LIST"));
        Set<String> postCollectionKeys = Set.of(IRedisConstant.REDIS_POST_ALL.concat(":page:0:size:10:sortBy:createdAt:sortDir:desc"));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(postRepository.findById(14)).thenReturn(Optional.of(post));
        when(bloggersRepository.findByEmail("reader@example.com")).thenReturn(Optional.of(blogger));
        when(modelMapper.map(request, Comment.class)).thenReturn(newComment);
        when(commentRepository.save(newComment)).thenReturn(savedComment);
        when(modelMapper.map(savedComment, CommentDto.class)).thenReturn(savedDto);
        when(commentRepository.findByPostId(14)).thenReturn(List.of(savedComment));
        when(commentRepository.findById(9)).thenReturn(Optional.of(savedComment));
        when(redisTemplate.keys(IRedisConstant.REDIS_COMMENT_POST.concat("14").concat("*"))).thenReturn(commentKeys);
        when(redisTemplate.keys(IRedisConstant.REDIS_POST.concat("*"))).thenReturn(postCollectionKeys);

        TransactionSynchronizationManager.initSynchronization();

        CommentDto result = service.createComment(request, 14, "reader@example.com");

        assertEquals(9, result.getId());
        verify(redisTemplate, never()).keys(anyString());

        triggerAfterCommit();

        verify(redisTemplate).delete(commentKeys);
        verify(valueOperations).set(eq(IRedisConstant.REDIS_COMMENT_POST.concat("14:LIST")), argThat(value -> value instanceof List<?> list && list.size() == 1));
        verify(valueOperations).set(eq(IRedisConstant.REDIS_COMMENT_POST.concat("14:ID:9")), eq(savedDto));
        verify(redisTemplate).delete(postCollectionKeys);
        verify(redisTemplate).delete(IRedisConstant.REDIS_POST_ID.concat("14"));
    }

    @Test
    void deleteByCommentIdRefreshesListCacheWithoutDeletedCommentDetail() {
        CommentServiceImpl service = new CommentServiceImpl(commentRepository, postRepository, bloggersRepository, modelMapper, redisTemplate);
        Post post = post(15);
        Blogger blogger = blogger("owner@example.com");
        Comment existingComment = comment(20, "Existing comment content", post, blogger);
        Set<String> commentKeys = Set.of(
                IRedisConstant.REDIS_COMMENT_POST.concat("15:LIST"),
                IRedisConstant.REDIS_COMMENT_POST.concat("15:ID:20")
        );
        Set<String> postCollectionKeys = Set.of(IRedisConstant.REDIS_POST_KEYWORD.concat("travel"));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(commentRepository.findById(20)).thenReturn(Optional.of(existingComment));
        when(commentRepository.findByPostId(15)).thenReturn(List.of());
        when(redisTemplate.keys(IRedisConstant.REDIS_COMMENT_POST.concat("15").concat("*"))).thenReturn(commentKeys);
        when(redisTemplate.keys(IRedisConstant.REDIS_POST.concat("*"))).thenReturn(postCollectionKeys);

        TransactionSynchronizationManager.initSynchronization();

        service.deleteByCommentId(20);
        triggerAfterCommit();

        verify(commentRepository).delete(existingComment);
        verify(redisTemplate).delete(commentKeys);
        verify(valueOperations).set(eq(IRedisConstant.REDIS_COMMENT_POST.concat("15:LIST")), argThat(value -> value instanceof List<?> list && list.isEmpty()));
        verify(valueOperations, never()).set(eq(IRedisConstant.REDIS_COMMENT_POST.concat("15:ID:20")), any());
        verify(redisTemplate).delete(postCollectionKeys);
        verify(redisTemplate).delete(IRedisConstant.REDIS_POST_ID.concat("15"));
    }

    private void triggerAfterCommit() {
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }
        TransactionSynchronizationManager.clearSynchronization();
    }

    private CommentDto commentDto(Integer id, String content) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(id);
        commentDto.setContent(content);
        commentDto.setName("Test User");
        commentDto.setEmail("reader@example.com");
        return commentDto;
    }

    private Comment comment(Integer id, String content, Post post, Blogger blogger) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setContent(content);
        comment.setPost(post);
        comment.setBlogger(blogger);
        return comment;
    }

    private Post post(Integer id) {
        Post post = new Post();
        post.setId(id);
        post.setTitle("Post " + id);
        post.setDescription("Post description long enough");
        post.setContent("Post content long enough");
        return post;
    }

    private Blogger blogger(String email) {
        Blogger blogger = new Blogger();
        blogger.setEmail(email);
        blogger.setFirstName("Test");
        blogger.setLastName("User");
        return blogger;
    }
}

