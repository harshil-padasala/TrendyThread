package com.blog.app.services.impl;

import com.blog.app.entities.Category;
import com.blog.app.entities.Post;
import com.blog.app.entities.User;
import com.blog.app.exceptions.ResourceNotFoundException;
import com.blog.app.payloads.PostDto;
import com.blog.app.repositories.CategoryRepository;
import com.blog.app.repositories.PostRepository;
import com.blog.app.repositories.UserRepository;
import com.blog.app.services.PostService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    public PostDto getPostByID(Integer postID) {
        Post post = this.postRepository.findById(postID)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post id", postID));

        return this.postToPostDto(post);
    }

    @Override
    public List<PostDto> getAllPosts() {
        List<Post> postList = this.postRepository.findAll();
        List<PostDto> postDtoList = postList.stream().map(this::postToPostDto).toList();

        return postDtoList;
    }

    @Override
    public List<PostDto> getAllPostsByCategory(Integer categoryID) {

        Category category = categoryRepository.findById(categoryID)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "Category ID", categoryID));

        List<Post> posts = this.postRepository.findByCategory(category);

        List<PostDto> postDtoList = posts.stream().map((this::postToPostDto)).toList();

        return postDtoList;
    }

    @Override
    public List<PostDto> getAllPostsByUser(Integer userID) {

        User user = this.userRepository.findById(userID)
                .orElseThrow(() -> new ResourceNotFoundException("User", "User Id", userID));

        List<Post> posts = this.postRepository.findByUser(user);

        List<PostDto> postDtoList = posts.stream().map((this::postToPostDto)).toList();

        return postDtoList;
    }

    @Override
    public PostDto createPost(PostDto postDto, Integer userID, Integer categoryId) {

        User user = userRepository.findById(userID)
                .orElseThrow(() -> new ResourceNotFoundException("User", "User ID", userID));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "Category ID", categoryId));

        Post post = postDtoToPost(postDto);
        post.setImageName("default");
        post.setCreatedDate(new Date());
        post.setCategory(category);
        post.setUser(user);

        Post newPost = this.postRepository.save(post);

        return postToPostDto(newPost);
    }

    @Override
    public PostDto updatePostById(Integer postID, PostDto postDto) {
        Post post = this.postRepository.findById(postID)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postID));
        post.setContent(postDto.getContent());
        post.setTitle(postDto.getTitle());
        post.setImageName(postDto.getImageName());
        post.setCreatedDate(postDto.getCreatedDate());

        Post savedPost = this.postRepository.save(post);
        return this.postToPostDto(savedPost);
    }

    @Override
    public void deletePostByID(Integer postID) {
        this.postRepository.findById(postID)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postID));

        this.postRepository.deleteById(postID);
    }

    private PostDto postToPostDto(Post post) {
        return this.modelMapper.map(post, PostDto.class);
    }

    private Post postDtoToPost(PostDto postDto) {
        return this.modelMapper.map(postDto, Post.class);
    }
}
