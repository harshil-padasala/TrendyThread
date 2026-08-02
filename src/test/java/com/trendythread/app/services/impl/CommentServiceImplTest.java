package com.trendythread.app.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trendythread.app.constants.IRedisConstant;
import com.trendythread.app.constants.IRedisTtlConstant;
import com.trendythread.app.dto.CommentDto;
import com.trendythread.app.entities.Blogger;
import com.trendythread.app.entities.Comment;
import com.trendythread.app.entities.Post;
import com.trendythread.app.exceptions.BlogAPIException;
import com.trendythread.app.repositories.BloggersRepository;
import com.trendythread.app.repositories.CommentRepository;
import com.trendythread.app.repositories.PostRepository;
import com.trendythread.app.services.NotificationService;
import com.trendythread.app.util.RedisCacheEvictionHelper;
import com.trendythread.app.util.RedisCacheSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
    private RedisCacheSupport redisCacheSupport;

    @Mock
    private RedisCacheEvictionHelper redisCacheEvictionHelper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private NotificationService notificationService;

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void findByPostIdReturnsCachedCommentsWithoutRepositoryLookup() {
        CommentServiceImpl service = new CommentServiceImpl(commentRepository, postRepository, bloggersRepository, modelMapper, redisCacheSupport, redisCacheEvictionHelper, objectMapper, notificationService);
        CommentDto cachedComment = commentDto(4, "Cached comment");

        when(redisCacheSupport.get(IRedisConstant.REDIS_COMMENT_POST.concat("12:LIST"))).thenReturn(List.of(cachedComment));
        when(objectMapper.convertValue(eq(List.of(cachedComment)), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of(cachedComment));

        List<CommentDto> result = service.findByPostId(12);

        assertEquals(1, result.size());
        assertEquals(4, result.get(0).getId());
        verify(commentRepository, never()).findByPostId(12);
    }

    @Test
    void createCommentEvictsCommentCachesRefreshesDetailAndEvictsRelatedPostCachesAfterCommit() {
        CommentServiceImpl service = new CommentServiceImpl(commentRepository, postRepository, bloggersRepository, modelMapper, redisCacheSupport, redisCacheEvictionHelper, objectMapper, notificationService);
        Post post = post(14);
        Blogger blogger = blogger("reader@example.com");
        CommentDto request = commentDto(null, "A thoughtful comment body");
        Comment newComment = comment(9, "A thoughtful comment body", post, blogger);
        Comment savedComment = comment(9, "A thoughtful comment body", post, blogger);
        CommentDto savedDto = commentDto(9, "A thoughtful comment body");

        when(postRepository.findById(14)).thenReturn(Optional.of(post));
        when(bloggersRepository.findByEmail("reader@example.com")).thenReturn(Optional.of(blogger));
        when(modelMapper.map(request, Comment.class)).thenReturn(newComment);
        when(commentRepository.save(newComment)).thenReturn(savedComment);
        when(modelMapper.map(savedComment, CommentDto.class)).thenReturn(savedDto);
        when(commentRepository.findById(9)).thenReturn(Optional.of(savedComment));

        TransactionSynchronizationManager.initSynchronization();

        CommentDto result = service.createComment(request, 14, "reader@example.com");

        assertEquals(9, result.getId());
        verify(redisCacheEvictionHelper, never()).evictCommentCachesForPost(any());

        triggerAfterCommit();

        verify(redisCacheEvictionHelper).evictCommentCachesForPost(14);
        verify(redisCacheSupport, never()).set(eq(IRedisConstant.REDIS_COMMENT_POST.concat("14:LIST")), any(), any());
        verify(redisCacheSupport).set(eq(IRedisConstant.REDIS_COMMENT_POST.concat("14:ID:9")), eq(savedDto), eq(IRedisTtlConstant.TTL_ENTITY));
        verify(redisCacheEvictionHelper).evictPostCollectionCaches();
        verify(redisCacheEvictionHelper).evictPostIdCaches(Set.of(14));
    }

    @Test
    void createCommentWithParentIdLinksToParentComment() {
        CommentServiceImpl service = new CommentServiceImpl(commentRepository, postRepository, bloggersRepository, modelMapper, redisCacheSupport, redisCacheEvictionHelper, objectMapper, notificationService);
        Post post = post(14);
        Blogger blogger = blogger("reader@example.com");
        Comment parent = comment(9, "Parent comment", post, blogger);
        CommentDto request = commentDto(null, "A reply body long enough");
        request.setParentId(9);
        Comment newComment = comment(10, "A reply body long enough", post, blogger);
        Comment savedComment = comment(10, "A reply body long enough", post, blogger);
        savedComment.setParentComment(parent);

        when(postRepository.findById(14)).thenReturn(Optional.of(post));
        when(bloggersRepository.findByEmail("reader@example.com")).thenReturn(Optional.of(blogger));
        when(commentRepository.findById(9)).thenReturn(Optional.of(parent));
        when(modelMapper.map(request, Comment.class)).thenReturn(newComment);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
        when(modelMapper.map(savedComment, CommentDto.class)).thenReturn(commentDto(10, "A reply body long enough"));

        TransactionSynchronizationManager.initSynchronization();
        CommentDto result = service.createComment(request, 14, "reader@example.com");
        triggerAfterCommit();

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertEquals(parent, captor.getValue().getParentComment());
        assertEquals(9, result.getParentId());
    }

    @Test
    void createCommentWithParentFromDifferentPostThrows() {
        CommentServiceImpl service = new CommentServiceImpl(commentRepository, postRepository, bloggersRepository, modelMapper, redisCacheSupport, redisCacheEvictionHelper, objectMapper, notificationService);
        Post post = post(14);
        Post otherPost = post(99);
        Blogger blogger = blogger("reader@example.com");
        Comment parentOnOtherPost = comment(9, "Parent on a different post", otherPost, blogger);
        CommentDto request = commentDto(null, "A reply body long enough");
        request.setParentId(9);

        when(postRepository.findById(14)).thenReturn(Optional.of(post));
        when(bloggersRepository.findByEmail("reader@example.com")).thenReturn(Optional.of(blogger));
        when(modelMapper.map(request, Comment.class)).thenReturn(new Comment());
        when(commentRepository.findById(9)).thenReturn(Optional.of(parentOnOtherPost));

        assertThrows(BlogAPIException.class, () -> service.createComment(request, 14, "reader@example.com"));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void findByPostIdAssemblesNestedReplyTree() {
        CommentServiceImpl service = new CommentServiceImpl(commentRepository, postRepository, bloggersRepository, modelMapper, redisCacheSupport, redisCacheEvictionHelper, objectMapper, notificationService);
        Post post = post(30);
        Blogger blogger = blogger("reader@example.com");

        Comment root = comment(1, "Root comment", post, blogger);
        Comment reply = comment(2, "Reply to root", post, blogger);
        reply.setParentComment(root);
        Comment replyToReply = comment(3, "Reply to reply", post, blogger);
        replyToReply.setParentComment(reply);

        when(redisCacheSupport.get(any())).thenReturn(null);
        when(commentRepository.findByPostId(30)).thenReturn(List.of(root, reply, replyToReply));
        when(modelMapper.map(root, CommentDto.class)).thenReturn(commentDto(1, "Root comment"));
        when(modelMapper.map(reply, CommentDto.class)).thenReturn(commentDto(2, "Reply to root"));
        when(modelMapper.map(replyToReply, CommentDto.class)).thenReturn(commentDto(3, "Reply to reply"));

        List<CommentDto> result = service.findByPostId(30);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        assertNull(result.get(0).getParentId());
        assertEquals(1, result.get(0).getReplies().size());
        assertEquals(2, result.get(0).getReplies().get(0).getId());
        assertEquals(1, result.get(0).getReplies().get(0).getReplies().size());
        assertEquals(3, result.get(0).getReplies().get(0).getReplies().get(0).getId());
    }

    @Test
    void deleteByCommentIdEvictsCommentCachesWithoutRefreshingDeletedCommentDetail() {
        CommentServiceImpl service = new CommentServiceImpl(commentRepository, postRepository, bloggersRepository, modelMapper, redisCacheSupport, redisCacheEvictionHelper, objectMapper, notificationService);
        Post post = post(15);
        Blogger blogger = blogger("owner@example.com");
        Comment existingComment = comment(20, "Existing comment content", post, blogger);

        when(commentRepository.findById(20)).thenReturn(Optional.of(existingComment));

        TransactionSynchronizationManager.initSynchronization();

        service.deleteByCommentId(20);
        triggerAfterCommit();

        verify(commentRepository).delete(existingComment);
        verify(redisCacheEvictionHelper).evictCommentCachesForPost(15);
        verify(redisCacheSupport, never()).set(eq(IRedisConstant.REDIS_COMMENT_POST.concat("15:LIST")), any(), any());
        verify(redisCacheSupport, never()).set(eq(IRedisConstant.REDIS_COMMENT_POST.concat("15:ID:20")), any(), any());
        verify(redisCacheEvictionHelper).evictPostCollectionCaches();
        verify(redisCacheEvictionHelper).evictPostIdCaches(Set.of(15));
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

