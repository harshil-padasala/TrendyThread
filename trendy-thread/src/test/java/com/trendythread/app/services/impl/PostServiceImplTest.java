package com.trendythread.app.services.impl;

import com.trendythread.app.constants.IRedisConstant;
import com.trendythread.app.dto.PostDto;
import com.trendythread.app.entities.Blogger;
import com.trendythread.app.entities.Category;
import com.trendythread.app.entities.Post;
import com.trendythread.app.payloads.PostResponse;
import com.trendythread.app.repositories.BloggersRepository;
import com.trendythread.app.repositories.CategoryRepository;
import com.trendythread.app.repositories.PostRepository;
import com.trendythread.app.services.CategoryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private BloggersRepository bloggersRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryService categoryService;

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
    void findByPostIdReturnsCachedPostWithoutHittingRepository() {
        PostServiceImpl service = new PostServiceImpl(postRepository, modelMapper, bloggersRepository, categoryRepository, categoryService, redisTemplate);
        PostDto cachedPost = postDto(9, "Cached title");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(IRedisConstant.REDIS_POST_ID.concat("9"))).thenReturn(cachedPost);

        PostDto result = service.findByPostId(9);

        assertEquals(9, result.getId());
        assertEquals("Cached title", result.getTitle());
        verify(postRepository, never()).findById(9);
    }

    @Test
    void createPostRefreshesPostIdCacheAndEvictsCollectionsAfterCommit() {
        PostServiceImpl service = new PostServiceImpl(postRepository, modelMapper, bloggersRepository, categoryRepository, categoryService, redisTemplate);
        PostDto request = postDto(null, "New post");
        Blogger blogger = blogger(4, "author@example.com");
        Category category = category(6, "Technology");
        Post transientPost = post(11, "New post", category, blogger);
        Post savedPost = post(11, "New post", category, blogger);
        PostDto savedDto = postDto(11, "New post");
        Set<String> collectionKeys = Set.of(IRedisConstant.REDIS_POST_ALL.concat(":page:0:size:10:sortBy:createdAt:sortDir:desc"));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(bloggersRepository.findByEmail("author@example.com")).thenReturn(Optional.of(blogger));
        when(categoryRepository.findById(6)).thenReturn(Optional.of(category));
        when(modelMapper.map(request, Post.class)).thenReturn(transientPost);
        when(postRepository.save(transientPost)).thenReturn(savedPost);
        when(modelMapper.map(savedPost, PostDto.class)).thenReturn(savedDto);
        when(postRepository.findAllById(Set.of(11))).thenReturn(List.of(savedPost));
        when(redisTemplate.keys(IRedisConstant.REDIS_POST.concat("*"))).thenReturn(collectionKeys);

        TransactionSynchronizationManager.initSynchronization();

        PostDto result = service.createPost(request, "author@example.com", 6);

        assertEquals(11, result.getId());
        verify(redisTemplate, never()).keys(anyString());

        triggerAfterCommit();

        verify(categoryService).updatePostCount(6);
        verify(redisTemplate).delete(collectionKeys);
        verify(valueOperations).set(IRedisConstant.REDIS_POST_ID.concat("11"), savedDto);
    }

    @Test
    void deleteByPostIdEvictsPostIdAndRelatedCommentCachesAfterCommit() {
        PostServiceImpl service = new PostServiceImpl(postRepository, modelMapper, bloggersRepository, categoryRepository, categoryService, redisTemplate);
        Blogger blogger = blogger(5, "writer@example.com");
        Category category = category(3, "Travel");
        Post existingPost = post(21, "Travel guide", category, blogger);
        Set<String> postCollectionKeys = Set.of(IRedisConstant.REDIS_POST_KEYWORD.concat("travel"));
        Set<String> commentKeys = Set.of(
                IRedisConstant.REDIS_COMMENT_POST.concat("21:LIST"),
                IRedisConstant.REDIS_COMMENT_POST.concat("21:ID:7")
        );

        when(postRepository.findById(21)).thenReturn(Optional.of(existingPost));
        when(redisTemplate.keys(IRedisConstant.REDIS_POST.concat("*"))).thenReturn(postCollectionKeys);
        when(redisTemplate.keys(IRedisConstant.REDIS_COMMENT_POST.concat("21").concat("*"))).thenReturn(commentKeys);

        TransactionSynchronizationManager.initSynchronization();

        service.deleteByPostId(21);
        triggerAfterCommit();

        verify(postRepository).delete(existingPost);
        verify(categoryService).updatePostCount(3);
        verify(redisTemplate).delete(postCollectionKeys);
        verify(redisTemplate).delete(argThat((Set<String> keys) -> keys.equals(Set.of(IRedisConstant.REDIS_POST_ID.concat("21")))));
        verify(redisTemplate).delete(commentKeys);
    }

    private void triggerAfterCommit() {
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }
        TransactionSynchronizationManager.clearSynchronization();
    }

    private PostDto postDto(Integer id, String title) {
        PostDto postDto = new PostDto();
        postDto.setId(id);
        postDto.setTitle(title);
        postDto.setDescription("Post description long enough");
        postDto.setContent("Post content long enough");
        return postDto;
    }

    private Post post(Integer id, String title, Category category, Blogger blogger) {
        Post post = new Post();
        post.setId(id);
        post.setTitle(title);
        post.setDescription("Post description long enough");
        post.setContent("Post content long enough");
        post.setCategory(category);
        post.setBlogger(blogger);
        return post;
    }

    private Blogger blogger(int id, String email) {
        Blogger blogger = new Blogger();
        blogger.setId(id);
        blogger.setEmail(email);
        blogger.setFirstName("Test");
        blogger.setLastName("Writer");
        return blogger;
    }

    private Category category(int id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDescription(name + " description");
        category.setFeatured(false);
        category.setDisplayOrder(0);
        category.setPostCount(0);
        category.setAutoSuggested(false);
        return category;
    }
}

