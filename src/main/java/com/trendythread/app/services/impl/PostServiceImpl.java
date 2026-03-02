package com.trendythread.app.services.impl;

import com.trendythread.app.entities.Blogger;
import com.trendythread.app.entities.Category;
import com.trendythread.app.entities.Post;
import com.trendythread.app.exceptions.ResourceNotFoundException;
import com.trendythread.app.dto.PostDto;
import com.trendythread.app.payloads.PostResponse;
import com.trendythread.app.repositories.CategoryRepository;
import com.trendythread.app.repositories.PostRepository;
import com.trendythread.app.repositories.BloggersRepository;
import com.trendythread.app.services.PostService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private BloggersRepository bloggersRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public PostDto findByPostId(Integer PostId) {
        log.info("findByPostId - request received: id={}", PostId);
        Post post = this.postRepository.findById(PostId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post id", PostId));

        PostDto dto = this.postToPostDto(post);
        log.debug("findByPostId - fetched post: {}", dto);
        return dto;
    }

    @Override
    public PostResponse findAllPosts(Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc) {
        log.info("findAllPosts - request received: pageNumber={}, pageSize={}, sortBy={}, isAsc={}", pageNumber, pageSize, sortBy, isAsc);
        Pageable pageable = isAsc ? PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending()) :
                PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).descending());

        Page<Post> pagePostList = this.postRepository.findAll(pageable);

        PostResponse response = this.generatePostAsPageResponse(pagePostList);
        log.debug("findAllPosts - returning page: pageNumber={}, pageSize={}, totalElements={}", response.getPageNumber(), response.getPageSize(), response.getTotalElements());
        return response;
    }

    @Override
    public PostResponse findPostsByCategoryId(Integer categoryID, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc) {
        log.info("findPostsByCategoryId - request received: categoryId={}, pageNumber={}, pageSize={}, sortBy={}, isAsc={}", categoryID, pageNumber, pageSize, sortBy, isAsc);

        Pageable pageable = isAsc ? PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending()) :
                PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).descending());

        Category category = categoryRepository.findById(categoryID)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "Category ID", categoryID));

        Page<Post> posts = this.postRepository.findByCategory(category, pageable);

        PostResponse response = this.generatePostAsPageResponse(posts);
        log.debug("findPostsByCategoryId - found {} posts for categoryId={}", response.getTotalElements(), categoryID);
        return response;
    }

    @Override
    public PostResponse findPostsByBloggerId(Integer bloggerID, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc) {
        log.info("findPostsByBloggerId - request received: bloggerID={}, pageNumber={}, pageSize={}, sortBy={}, isAsc={}", bloggerID, pageNumber, pageSize, sortBy, isAsc);

        Pageable pageable = isAsc ? PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending()) :
                PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).descending());

        Blogger blogger = this.bloggersRepository.findById(bloggerID)
                .orElseThrow(() -> new ResourceNotFoundException("Blogger", "Blogger Id", bloggerID));

        Page<Post> posts = this.postRepository.findByBlogger(blogger, pageable);

        PostResponse response = this.generatePostAsPageResponse(posts);
        log.debug("findPostsByBloggerId - found {} posts for findPostsByBloggerId={}", response.getTotalElements(), bloggerID);
        return response;
    }

    @Override
    public PostDto createPost(PostDto postDto, Integer bloggerID, Integer categoryId) {
        log.info("createPost - request received: bloggerId={}, categoryId={}, postDto={}", bloggerID, categoryId, postDto);

        Blogger blogger = bloggersRepository.findById(bloggerID)
                .orElseThrow(() -> new ResourceNotFoundException("Blogger", "Blogger ID", bloggerID));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "Category ID", categoryId));

        Post post = postDtoToPost(postDto);
        post.setCategory(category);
        post.setBlogger(blogger);

        Post newPost = this.postRepository.save(post);

        PostDto dto = postToPostDto(newPost);
        log.info("createPost - created post id={}", dto.getId());
        return dto;
    }

    @Override
    public PostDto updateByPostId(Integer postID, PostDto postDto) {
        log.info("updateByPostId - request received: postId={}, postDto={}", postID, postDto);
        Post post = this.postRepository.findById(postID)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postID));
        post.setContent(postDto.getContent());
        post.setTitle(postDto.getTitle());

        Post savedPost = this.postRepository.save(post);
        PostDto dto = this.postToPostDto(savedPost);
        log.info("updateByPostId - update successful: id={}", dto.getId());
        return dto;
    }

    @Override
    public void deleteByPostId(Integer postID) {
        log.info("deleteByPostId - request received: id={}", postID);
        this.postRepository.findById(postID)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postID));

        this.postRepository.deleteById(postID);
        log.info("deleteByPostId - deleted post id={}", postID);
    }

    @Override
    public PostResponse searchPost(String keyword, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc) {
        log.info("searchPost - request received: keyword={}, pageNumber={}, pageSize={}, sortBy={}, isAsc={}", keyword, pageNumber, pageSize, sortBy, isAsc);
        Pageable pageable = isAsc ? PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending()) :
                PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).descending());
        Page<Post> postList = this.postRepository.findByTitleContaining(keyword, pageable);

        PostResponse response = this.generatePostAsPageResponse(postList);
        log.debug("searchPost - found {} posts matching '{}'", response.getTotalElements(), keyword);
        return response;
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
        return this.modelMapper.map(post, PostDto.class);
    }

    private Post postDtoToPost(PostDto postDto) {
        return this.modelMapper.map(postDto, Post.class);
    }
}
