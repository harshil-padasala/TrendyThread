package com.trendythread.app.services.impl;

import com.trendythread.app.entities.Category;
import com.trendythread.app.entities.Post;
import com.trendythread.app.entities.User;
import com.trendythread.app.exceptions.ResourceNotFoundException;
import com.trendythread.app.dto.PostDto;
import com.trendythread.app.payloads.PostResponse;
import com.trendythread.app.repositories.CategoryRepository;
import com.trendythread.app.repositories.PostRepository;
import com.trendythread.app.repositories.UserRepository;
import com.trendythread.app.services.PostService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public PostDto findByPostId(Integer PostId) {
        Post post = this.postRepository.findById(PostId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post id", PostId));

        return this.postToPostDto(post);
    }

    @Override
    public PostResponse findAllPosts(Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc) {
        Pageable pageable = isAsc ? PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending()) :
                PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).descending());

        List<Post> list = this.postRepository.findAll();

        Page<Post> pagePostList = this.postRepository.findAll(pageable);
//        List<Post> postList = pagePostList.getContent();
//        List<PostDto> postDtoList = pagePostList.stream().map(this::postToPostDto).toList();

        return this.generatePostAsPageResponse(pagePostList);
    }

    @Override
    public PostResponse findPostsByCategoryId(Integer categoryID, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc) {

        Pageable pageable = isAsc ? PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending()) :
                PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).descending());

        Category category = categoryRepository.findById(categoryID)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "Category ID", categoryID));

        Page<Post> posts = this.postRepository.findByCategory(category, pageable);
//        List<Post> posts = this.postRepository.findByCategory(category);
//        List<PostDto> postDtoList = posts.stream().map((this::postToPostDto)).toList();

        return this.generatePostAsPageResponse(posts);
    }

    @Override
    public PostResponse findPostsByUserId(Integer userID, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc) {

        Pageable pageable = isAsc ? PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending()) :
                PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).descending());

        User user = this.userRepository.findById(userID)
                .orElseThrow(() -> new ResourceNotFoundException("User", "User Id", userID));

        Page<Post> posts = this.postRepository.findByUser(user, pageable);
//        List<Post> postList = posts.getContent();
//        List<PostDto> postDtoList = posts.stream().map((this::postToPostDto)).toList();

        return this.generatePostAsPageResponse(posts);
    }

    @Override
    public PostDto createPost(PostDto postDto, Integer userID, Integer categoryId) {

        User user = userRepository.findById(userID)
                .orElseThrow(() -> new ResourceNotFoundException("User", "User ID", userID));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "Category ID", categoryId));

        Post post = postDtoToPost(postDto);
        post.setCategory(category);
        post.setUser(user);

        Post newPost = this.postRepository.save(post);

        return postToPostDto(newPost);
    }

    @Override
    public PostDto updateByPostId(Integer postID, PostDto postDto) {
        Post post = this.postRepository.findById(postID)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postID));
        post.setContent(postDto.getContent());
        post.setTitle(postDto.getTitle());

        Post savedPost = this.postRepository.save(post);
        return this.postToPostDto(savedPost);
    }

    @Override
    public void deleteByPostId(Integer postID) {
        this.postRepository.findById(postID)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postID));

        this.postRepository.deleteById(postID);
    }

    @Override
    public PostResponse searchPost(String keyword, Integer pageNumber, Integer pageSize, String sortBy, boolean isAsc) {
        Pageable pageable = isAsc ? PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending()) :
                PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).descending());
        Page<Post> postList = this.postRepository.findByTitleContaining(keyword, pageable);

        return this.generatePostAsPageResponse(postList);
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
