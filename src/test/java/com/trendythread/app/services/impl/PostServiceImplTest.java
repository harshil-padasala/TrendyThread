package com.trendythread.app.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trendythread.app.constants.IRedisConstant;
import com.trendythread.app.constants.IRedisTtlConstant;
import com.trendythread.app.dto.PostDto;
import com.trendythread.app.entities.Blogger;
import com.trendythread.app.entities.Category;
import com.trendythread.app.entities.Post;
import com.trendythread.app.payloads.PostResponse;
import com.trendythread.app.repositories.BloggersRepository;
import com.trendythread.app.repositories.CategoryRepository;
import com.trendythread.app.repositories.PostRepository;
import com.trendythread.app.services.CategoryService;
import com.trendythread.app.util.RedisCacheEvictionHelper;
import com.trendythread.app.util.RedisCacheSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
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
    private RedisCacheSupport redisCacheSupport;

    @Mock
    private RedisCacheEvictionHelper redisCacheEvictionHelper;

    @Mock
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void findByPostIdReturnsCachedPostWithoutHittingRepository() {
        PostServiceImpl service = new PostServiceImpl(postRepository, modelMapper, bloggersRepository, categoryRepository, categoryService, redisCacheSupport, redisCacheEvictionHelper, objectMapper);
        PostDto cachedPost = postDto(9, "Cached title");

        when(redisCacheSupport.get(IRedisConstant.REDIS_POST_ID.concat("9"))).thenReturn(cachedPost);
        when(objectMapper.convertValue(cachedPost, PostDto.class)).thenReturn(cachedPost);

        PostDto result = service.findByPostId(9);

        assertEquals(9, result.getId());
        assertEquals("Cached title", result.getTitle());
        verify(postRepository, never()).findById(9);
    }

    @Test
    void createPostRefreshesPostIdCacheAndEvictsCollectionsAfterCommit() {
        PostServiceImpl service = new PostServiceImpl(postRepository, modelMapper, bloggersRepository, categoryRepository, categoryService, redisCacheSupport, redisCacheEvictionHelper, objectMapper);
        PostDto request = postDto(null, "New post");
        Blogger blogger = blogger(4, "author@example.com");
        Category category = category(6, "Technology");
        Post transientPost = post(11, "New post", category, blogger);
        Post savedPost = post(11, "New post", category, blogger);
        PostDto savedDto = postDto(11, "New post");

        when(bloggersRepository.findByEmail("author@example.com")).thenReturn(Optional.of(blogger));
        when(categoryRepository.findById(6)).thenReturn(Optional.of(category));
        when(modelMapper.map(request, Post.class)).thenReturn(transientPost);
        when(postRepository.save(transientPost)).thenReturn(savedPost);
        when(modelMapper.map(savedPost, PostDto.class)).thenReturn(savedDto);
        when(postRepository.findAllById(Set.of(11))).thenReturn(List.of(savedPost));

        TransactionSynchronizationManager.initSynchronization();

        PostDto result = service.createPost(request, "author@example.com", 6);

        assertEquals(11, result.getId());
        verify(redisCacheEvictionHelper, never()).evictPostCollectionCaches();

        triggerAfterCommit();

        verify(categoryService).updatePostCount(6);
        verify(redisCacheEvictionHelper).evictPostCollectionCaches();
        verify(redisCacheSupport).set(IRedisConstant.REDIS_POST_ID.concat("11"), savedDto, IRedisTtlConstant.TTL_ENTITY);
    }

    @Test
    void deleteByPostIdEvictsPostIdAndRelatedCommentCachesAfterCommit() {
        PostServiceImpl service = new PostServiceImpl(postRepository, modelMapper, bloggersRepository, categoryRepository, categoryService, redisCacheSupport, redisCacheEvictionHelper, objectMapper);
        Blogger blogger = blogger(5, "writer@example.com");
        Category category = category(3, "Travel");
        Post existingPost = post(21, "Travel guide", category, blogger);

        when(postRepository.findById(21)).thenReturn(Optional.of(existingPost));

        TransactionSynchronizationManager.initSynchronization();

        service.deleteByPostId(21);
        triggerAfterCommit();

        verify(postRepository).delete(existingPost);
        verify(categoryService).updatePostCount(3);
        verify(redisCacheEvictionHelper).evictPostCollectionCaches();
        verify(redisCacheEvictionHelper).evictPostIdCaches(Set.of(21));
        verify(redisCacheEvictionHelper).evictCommentCachesForPost(21);
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

