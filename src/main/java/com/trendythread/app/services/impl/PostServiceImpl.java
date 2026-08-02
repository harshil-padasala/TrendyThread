package com.trendythread.app.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trendythread.app.constants.IRedisConstant;
import com.trendythread.app.constants.IRedisTtlConstant;
import com.trendythread.app.entities.Blogger;
import com.trendythread.app.entities.Category;
import com.trendythread.app.entities.Post;
import com.trendythread.app.entities.Tag;
import com.trendythread.app.exceptions.ResourceNotFoundException;
import com.trendythread.app.dto.PostDto;
import com.trendythread.app.dto.PostViewCountDto;
import com.trendythread.app.dto.TrendingPostDto;
import com.trendythread.app.payloads.PostResponse;
import com.trendythread.app.repositories.CategoryRepository;
import com.trendythread.app.repositories.PostRepository;
import com.trendythread.app.repositories.BloggersRepository;
import com.trendythread.app.services.CategoryService;
import com.trendythread.app.services.FollowService;
import com.trendythread.app.services.PostService;
import com.trendythread.app.services.PostViewService;
import com.trendythread.app.services.TagService;
import com.trendythread.app.util.RedisCacheEvictionHelper;
import com.trendythread.app.util.RedisCacheSupport;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    private final ModelMapper modelMapper;

    private final BloggersRepository bloggersRepository;

    private final CategoryRepository categoryRepository;

    private final CategoryService categoryService;

    private final TagService tagService;

    private final PostViewService postViewService;

    private final FollowService followService;

    private final RedisCacheSupport redisCacheSupport;

    private final RedisCacheEvictionHelper redisCacheEvictionHelper;

    private final ObjectMapper objectMapper;

    @Autowired
    public PostServiceImpl(PostRepository postRepository,
                           ModelMapper modelMapper,
                           BloggersRepository bloggersRepository,
                           CategoryRepository categoryRepository,
                           CategoryService categoryService,
                           TagService tagService,
                           PostViewService postViewService,
                           FollowService followService,
                           RedisCacheSupport redisCacheSupport,
                           RedisCacheEvictionHelper redisCacheEvictionHelper,
                           ObjectMapper objectMapper) {
        this.postRepository = postRepository;
        this.modelMapper = modelMapper;
        this.bloggersRepository = bloggersRepository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.postViewService = postViewService;
        this.followService = followService;
        this.redisCacheSupport = redisCacheSupport;
        this.redisCacheEvictionHelper = redisCacheEvictionHelper;
        this.objectMapper = objectMapper;
    }

    @Override
    public PostDto findByPostId(Integer PostId) {
        log.info("findByPostId - request received: id={}", PostId);

        postViewService.recordView(PostId);

        String cacheKey = IRedisConstant.REDIS_POST_ID.concat(String.valueOf(PostId));
        Object cachedPost = redisCacheSupport.get(cacheKey);
        if (cachedPost != null) {
            log.info("findByPostId - cache hit for id={}", PostId);
            return objectMapper.convertValue(cachedPost, PostDto.class);
        }

        Post post = this.postRepository.findById(PostId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post id", PostId));

        PostDto dto = this.postToPostDto(post);
        redisCacheSupport.set(cacheKey, dto, IRedisTtlConstant.TTL_ENTITY);
        log.debug("findByPostId - fetched post: {}", dto);
        return dto;
    }

    @Override
    public PostViewCountDto getViewCount(Integer postId) {
        log.info("getViewCount - request received: postId={}", postId);
        return new PostViewCountDto(postViewService.getViewCount(postId));
    }

    @Override
    public List<TrendingPostDto> getTrendingPosts(int limit) {
        log.info("getTrendingPosts - request received: limit={}", limit);

        List<Integer> trendingIds = postViewService.getTrendingPostIds(limit);
        if (trendingIds.isEmpty()) {
            return List.of();
        }

        Map<Integer, Post> postsById = postRepository.findAllById(trendingIds).stream()
                .collect(Collectors.toMap(Post::getId, p -> p));

        List<TrendingPostDto> result = new ArrayList<>();
        for (Integer postId : trendingIds) {
            Post post = postsById.get(postId);
            if (post == null) {
                continue; // post was deleted but its view-count entry is still in Redis
            }
            result.add(new TrendingPostDto(postToPostDto(post), postViewService.getViewCount(postId)));
        }

        log.debug("getTrendingPosts - returning {} trending posts", result.size());
        return result;
    }

    @Override
    public PostResponse findAllPosts(Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc) {
        log.info("findAllPosts - request received: pageNumber={}, pageSize={}, sortBy={}, isAsc={}", pageNumber, pageSize, sortBy, isAsc);

        String cacheKey = IRedisConstant.REDIS_POST_ALL.concat(buildPageCacheSuffix(pageNumber, pageSize, sortBy, isAsc));
        Object cachedResponse = redisCacheSupport.get(cacheKey);
        if (cachedResponse != null) {
            log.info("findAllPosts - cache hit for key={}", cacheKey);
            return objectMapper.convertValue(cachedResponse, PostResponse.class);
        }

        Pageable pageable = isAsc ? PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending()) :
                PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).descending());

        Page<Post> pagePostList = this.postRepository.findAll(pageable);

        PostResponse response = this.generatePostAsPageResponse(pagePostList);
        redisCacheSupport.set(cacheKey, response, IRedisTtlConstant.TTL_QUERY);
        log.debug("findAllPosts - returning page: pageNumber={}, pageSize={}, totalElements={}", response.getPageNumber(), response.getPageSize(), response.getTotalElements());
        return response;
    }

    @Override
    public PostResponse getLatestPosts(Integer limit) {
        log.info("getLatestPosts - request received: limit={}", limit);

        String cacheKey = IRedisConstant.REDIS_POST_LATEST
                .concat(IRedisConstant.REDIS_KEY_LIMIT_CONSTANT)
                .concat(String.valueOf(limit));
        Object cachedResponse = redisCacheSupport.get(cacheKey);
        if (cachedResponse != null) {
            log.info("getLatestPosts - cache hit for limit={}", limit);
            return objectMapper.convertValue(cachedResponse, PostResponse.class);
        }

        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        Page<Post> pagePostList = this.postRepository.findAll(pageable);

        PostResponse response = this.generatePostAsPageResponse(pagePostList);
        redisCacheSupport.set(cacheKey, response, IRedisTtlConstant.TTL_QUERY);
        log.debug("getLatestPosts - returning {} latest posts", response.getContent().size());
        return response;
    }

    @Override
    public PostResponse findPostsByCategoryId(Integer categoryID, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc) {
        log.info("findPostsByCategoryId - request received: categoryId={}, pageNumber={}, pageSize={}, sortBy={}, isAsc={}", categoryID, pageNumber, pageSize, sortBy, isAsc);

        String cacheKey = IRedisConstant.REDIS_POST_CATEGORY
                .concat(String.valueOf(categoryID))
                .concat(buildPageCacheSuffix(pageNumber, pageSize, sortBy, isAsc));
        Object cachedResponse = redisCacheSupport.get(cacheKey);
        if (cachedResponse != null) {
            log.info("findPostsByCategoryId - cache hit for categoryId={}", categoryID);
            return objectMapper.convertValue(cachedResponse, PostResponse.class);
        }

        Pageable pageable = isAsc ? PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending()) :
                PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).descending());

        Category category = categoryRepository.findById(categoryID)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "Category ID", categoryID));

        Page<Post> posts = this.postRepository.findByCategory(category, pageable);

        PostResponse response = this.generatePostAsPageResponse(posts);
        redisCacheSupport.set(cacheKey, response, IRedisTtlConstant.TTL_QUERY);
        log.debug("findPostsByCategoryId - found {} posts for categoryId={}", response.getTotalElements(), categoryID);
        return response;
    }

    @Override
    public PostResponse findPostsByTag(String tagName, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc) {
        log.info("findPostsByTag - request received: tagName={}, pageNumber={}, pageSize={}, sortBy={}, isAsc={}", tagName, pageNumber, pageSize, sortBy, isAsc);

        Pageable pageable = isAsc ? PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending()) :
                PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).descending());

        Page<Post> posts = this.postRepository.findByTags_NameIgnoreCase(tagName.trim().toLowerCase(), pageable);

        PostResponse response = this.generatePostAsPageResponse(posts);
        log.debug("findPostsByTag - found {} posts for tagName={}", response.getTotalElements(), tagName);
        return response;
    }

    @Override
    public PostResponse getFeed(String authenticatedUserEmail, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc) {
        log.info("getFeed - request received: user={}, pageNumber={}, pageSize={}, sortBy={}, isAsc={}", authenticatedUserEmail, pageNumber, pageSize, sortBy, isAsc);

        List<Integer> followingIds = followService.getFollowingIds(authenticatedUserEmail);
        if (followingIds.isEmpty()) {
            PostResponse empty = new PostResponse();
            empty.setContent(List.of());
            empty.setPageNumber(pageNumber);
            empty.setPageSize(pageSize);
            empty.setLastPage(true);
            return empty;
        }

        Pageable pageable = isAsc ? PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending()) :
                PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).descending());

        Page<Post> posts = this.postRepository.findByBlogger_IdIn(followingIds, pageable);

        PostResponse response = this.generatePostAsPageResponse(posts);
        log.debug("getFeed - found {} posts for user={}", response.getTotalElements(), authenticatedUserEmail);
        return response;
    }

    @Override
    public PostResponse findPostsByBloggerId(String authenticatedUserEmail, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc) {
        log.info("findPostsByBloggerId - request received: bloggerID={}, pageNumber={}, pageSize={}, sortBy={}, isAsc={}", authenticatedUserEmail, pageNumber, pageSize, sortBy, isAsc);

        String cacheKey = IRedisConstant.REDIS_POST_BLOGGER
                .concat(authenticatedUserEmail)
                .concat(buildPageCacheSuffix(pageNumber, pageSize, sortBy, isAsc));
        Object cachedResponse = redisCacheSupport.get(cacheKey);
        if (cachedResponse != null) {
            log.info("findPostsByBloggerId - cache hit for blogger={}", authenticatedUserEmail);
            return objectMapper.convertValue(cachedResponse, PostResponse.class);
        }

        Pageable pageable = isAsc ? PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending()) :
                PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).descending());

        Blogger blogger = this.bloggersRepository.findByEmail(authenticatedUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Blogger", "Blogger Id", authenticatedUserEmail));

        Page<Post> posts = this.postRepository.findByBlogger(blogger, pageable);

        PostResponse response = this.generatePostAsPageResponse(posts);
        redisCacheSupport.set(cacheKey, response, IRedisTtlConstant.TTL_QUERY);
        log.debug("findPostsByBloggerId - found {} posts for findPostsByBloggerId={}", response.getTotalElements(), authenticatedUserEmail);
        return response;
    }

    @Override
    public PostResponse findPostsByUserId(Integer userId, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc) {
        log.info("findPostsByUserId - request received: userId={}, pageNumber={}, pageSize={}, sortBy={}, isAsc={}", userId, pageNumber, pageSize, sortBy, isAsc);

        String cacheKey = IRedisConstant.REDIS_POST_USER
                .concat(String.valueOf(userId))
                .concat(buildPageCacheSuffix(pageNumber, pageSize, sortBy, isAsc));
        Object cachedResponse = redisCacheSupport.get(cacheKey);
        if (cachedResponse != null) {
            log.info("findPostsByUserId - cache hit for userId={}", userId);
            return objectMapper.convertValue(cachedResponse, PostResponse.class);
        }

        Pageable pageable = isAsc ? PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending()) :
                PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).descending());

        Blogger blogger = this.bloggersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Blogger", "Blogger Id", userId));

        Page<Post> posts = this.postRepository.findByBlogger(blogger, pageable);

        PostResponse response = this.generatePostAsPageResponse(posts);
        redisCacheSupport.set(cacheKey, response, IRedisTtlConstant.TTL_QUERY);
        log.debug("findPostsByUserId - found {} posts for userId={}", response.getTotalElements(), userId);
        return response;
    }

    @Override
    @Transactional
    public PostDto createPost(PostDto postDto, String authenticatedUserEmail, Integer categoryId) {
        log.info("createPost - request received: bloggerId={}, categoryId={}, postDto={}", authenticatedUserEmail, categoryId, postDto);

        Blogger blogger = bloggersRepository.findByEmail(authenticatedUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Blogger", "Blogger ID", authenticatedUserEmail));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "Category ID", categoryId));

        Post post = postDtoToPost(postDto);
        post.setCategory(category);
        post.setBlogger(blogger);
        post.setTags(tagService.resolveOrCreateTags(postDto.getTags()));

        Post newPost = this.postRepository.save(post);

        // NEW: Update category post count
        categoryService.updatePostCount(categoryId);

        PostDto dto = postToPostDto(newPost);
        schedulePostCacheRefreshAfterCommit(Set.of(newPost.getId()), Set.of());
        log.info("createPost - created post id={}", dto.getId());
        return dto;
    }

    @Override
    @Transactional
    public PostDto updateByPostId(Integer postID, PostDto postDto, String authenticatedUserEmail) {
        log.info("updateByPostId - request received: postId={}, postDto={}, authenticatedUser={}", postID, postDto, authenticatedUserEmail);
        Post post = this.postRepository.findById(postID)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postID));
        
        // Validate ownership - only the post creator can update their own post
        if (post.getBlogger() == null || !post.getBlogger().getEmail().equals(authenticatedUserEmail)) {
            log.warn("updateByPostId - FORBIDDEN: user {} attempted to update post {} owned by {}", 
                    authenticatedUserEmail, postID, post.getBlogger() != null ? post.getBlogger().getEmail() : "unknown");
            throw new com.trendythread.app.exceptions.BlogAPIException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "You can only update your own posts"
            );
        }
        
        post.setContent(postDto.getContent());
        post.setDescription(postDto.getDescription());
        post.setTitle(postDto.getTitle());
        if (postDto.getTags() != null) {
            post.setTags(tagService.resolveOrCreateTags(postDto.getTags()));
        }

        Post savedPost = this.postRepository.save(post);
        PostDto dto = this.postToPostDto(savedPost);
        schedulePostCacheRefreshAfterCommit(Set.of(savedPost.getId()), Set.of());
        log.info("updateByPostId - update successful: id={}", dto.getId());
        return dto;
    }

    @Override
    @Transactional
    public void deleteByPostId(Integer postID) {
        log.info("deleteByPostId - request received: id={}", postID);
        Post post = this.postRepository.findById(postID)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postID));

        this.postRepository.delete(post);

        // NEW: Update category post count
        categoryService.updatePostCount(post.getCategory().getId());

        schedulePostCacheRefreshAfterCommit(Set.of(), Set.of(postID));

        Integer deletedPostId = post.getId();
        scheduleAfterCommit(() -> redisCacheEvictionHelper.evictCommentCachesForPost(deletedPostId));

        log.info("deleteByPostId - deleted post id={}", postID);
    }

    @Override
    public PostResponse searchPost(String keyword, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc) {
        log.info("searchPost - request received: keyword={}, pageNumber={}, pageSize={}, sortBy={}, isAsc={}", keyword, pageNumber, pageSize, sortBy, isAsc);

        String cacheKey = IRedisConstant.REDIS_POST_KEYWORD
                .concat(keyword)
                .concat(buildPageCacheSuffix(pageNumber, pageSize, sortBy, isAsc));
        Object cachedResponse = redisCacheSupport.get(cacheKey);
        if (cachedResponse != null) {
            log.info("searchPost - cache hit for keyword={}", keyword);
            return objectMapper.convertValue(cachedResponse, PostResponse.class);
        }

        Pageable pageable = isAsc ? PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending()) :
                PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).descending());
        Page<Post> postList = this.postRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable);

        PostResponse response = this.generatePostAsPageResponse(postList);
        redisCacheSupport.set(cacheKey, response, IRedisTtlConstant.TTL_QUERY);
        log.debug("searchPost - found {} posts matching '{}'", response.getTotalElements(), keyword);
        return response;
    }

    private void schedulePostCacheRefreshAfterCommit(Set<Integer> postIdsToRefresh, Set<Integer> postIdsToDelete) {
        scheduleAfterCommit(() -> {
            redisCacheEvictionHelper.evictPostCollectionCaches();
            redisCacheEvictionHelper.evictPostIdCaches(postIdsToDelete);
            refreshPostIdCaches(postIdsToRefresh);
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

    private void refreshPostIdCaches(Set<Integer> postIdsToRefresh) {
        if (postIdsToRefresh == null || postIdsToRefresh.isEmpty()) {
            return;
        }

        for (Post post : postRepository.findAllById(postIdsToRefresh)) {
            redisCacheSupport.set(
                    IRedisConstant.REDIS_POST_ID.concat(String.valueOf(post.getId())),
                    postToPostDto(post),
                    IRedisTtlConstant.TTL_ENTITY
            );
        }

        log.debug("refreshPostIdCaches - refreshed {} post id cache entries", postIdsToRefresh.size());
    }

    private String buildPageCacheSuffix(Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc) {
        return IRedisConstant.REDIS_KEY_PAGE_CONSTANT.concat(String.valueOf(pageNumber))
                .concat(IRedisConstant.REDIS_KEY_SIZE_CONSTANT).concat(String.valueOf(pageSize))
                .concat(IRedisConstant.REDIS_KEY_SORT_BY_CONSTANT).concat(sortBy)
                .concat(IRedisConstant.REDIS_KEY_SORT_DIRECTION_CONSTANT).concat(isAsc ? "asc" : "desc");
    }

    private PostResponse generatePostAsPageResponse(Page<Post> posts) {
        PostResponse postResponse = new PostResponse();
        // Passing List<Post>, Converting it into List<PostDto>, and set it in PostResponse
        postResponse.setContent(posts.getContent().stream().map(this::postToPostDto).toList());
        postResponse.setPageNumber(posts.getNumber());
        postResponse.setPageSize(posts.getSize());
        postResponse.setTotalPages(posts.getTotalPages());
        postResponse.setTotalElements((int) posts.getTotalElements());
        postResponse.setLastPage(posts.isLast());

        return postResponse;
    }


    private PostDto postToPostDto(Post post) {
        PostDto dto = this.modelMapper.map(post, PostDto.class);
        // ModelMapper can't auto-project Set<Tag> -> Set<String>; set explicitly.
        dto.setTags(post.getTags() == null ? Set.of() :
                post.getTags().stream().map(Tag::getName).collect(Collectors.toCollection(java.util.LinkedHashSet::new)));
        return dto;
    }

    private Post postDtoToPost(PostDto postDto) {
        Post post = this.modelMapper.map(postDto, Post.class);
        // tags are resolved/assigned separately by the caller (create/update) via TagService
        post.setTags(new java.util.HashSet<>());
        return post;
    }
}
