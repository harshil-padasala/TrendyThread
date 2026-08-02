package com.trendythread.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trendythread.app.dto.PostDto;
import com.trendythread.app.dto.PostViewCountDto;
import com.trendythread.app.dto.TrendingPostDto;
import com.trendythread.app.exceptions.ResourceNotFoundException;
import com.trendythread.app.exceptions.GlobalExceptionHandler;
import com.trendythread.app.payloads.PostResponse;
import com.trendythread.app.services.PostService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Principal PRINCIPAL = () -> "john.doe@example.com";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(postController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private PostDto validPostDto() {
        PostDto dto = new PostDto();
        dto.setTitle("A valid post title");
        dto.setDescription("A valid post description long enough");
        dto.setContent("Some content that is long enough to pass validation");
        return dto;
    }

    @Test
    void createPostReturns201WithCreatedPost() throws Exception {
        PostDto request = validPostDto();
        PostDto saved = validPostDto();
        saved.setId(1);
        when(postService.createPost(any(PostDto.class), eq("john.doe@example.com"), eq(5))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/posts/category/5")
                        .principal(PRINCIPAL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createPostWithInvalidTitleReturns400() throws Exception {
        PostDto request = validPostDto();
        request.setTitle("no"); // below min size of 4

        mockMvc.perform(post("/api/v1/posts/category/5")
                        .principal(PRINCIPAL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fetchByPostIdReturnsPost() throws Exception {
        PostDto post = validPostDto();
        post.setId(7);
        when(postService.findByPostId(7)).thenReturn(post);

        mockMvc.perform(get("/api/v1/posts/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void fetchByPostIdNotFoundReturns404() throws Exception {
        when(postService.findByPostId(999)).thenThrow(new ResourceNotFoundException("Post", "postId", 999));

        mockMvc.perform(get("/api/v1/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void fetchAllPostsReturnsPaginatedResponse() throws Exception {
        PostResponse response = new PostResponse();
        response.setPageNumber(0);
        response.setPageSize(10);
        when(postService.findAllPosts(0, 10, "postId", true)).thenReturn(response);

        mockMvc.perform(get("/api/v1/posts")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "postId")
                        .param("sortDir", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    void getLatestPostsReturnsResponse() throws Exception {
        PostResponse response = new PostResponse();
        response.setContent(Collections.emptyList());
        when(postService.getLatestPosts(6)).thenReturn(response);

        mockMvc.perform(get("/api/v1/posts/latest"))
                .andExpect(status().isOk());

        verify(postService).getLatestPosts(6);
    }

    @Test
    void fetchByCategoryIdReturnsResponse() throws Exception {
        PostResponse response = new PostResponse();
        when(postService.findPostsByCategoryId(eq(3), anyInt(), anyInt(), anyString(), anyBoolean())).thenReturn(response);

        mockMvc.perform(get("/api/v1/posts/category/3"))
                .andExpect(status().isOk());
    }

    @Test
    void fetchByBloggerIdUsesAuthenticatedPrincipal() throws Exception {
        PostResponse response = new PostResponse();
        when(postService.findPostsByBloggerId(eq("john.doe@example.com"), anyInt(), anyInt(), anyString(), anyBoolean()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/posts/blogger").principal(PRINCIPAL))
                .andExpect(status().isOk());
    }

    @Test
    void fetchByUserIdReturnsResponse() throws Exception {
        PostResponse response = new PostResponse();
        when(postService.findPostsByUserId(eq(2), anyInt(), anyInt(), anyString(), anyBoolean())).thenReturn(response);

        mockMvc.perform(get("/api/v1/posts/user/2"))
                .andExpect(status().isOk());
    }

    @Test
    void updateByPostIdReturnsUpdatedPost() throws Exception {
        PostDto request = validPostDto();
        PostDto updated = validPostDto();
        updated.setId(7);
        when(postService.updateByPostId(eq(7), any(PostDto.class), eq("john.doe@example.com"))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/posts/7")
                        .principal(PRINCIPAL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void deleteByPostIdReturnsSuccessResponse() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(postService).deleteByPostId(7);
    }

    @Test
    void getTrendingPostsReturnsList() throws Exception {
        PostDto postDto = validPostDto();
        postDto.setId(3);
        when(postService.getTrendingPosts(5)).thenReturn(List.of(new TrendingPostDto(postDto, 42L)));

        mockMvc.perform(get("/api/v1/posts/trending").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].viewCount").value(42))
                .andExpect(jsonPath("$[0].post.id").value(3));
    }

    @Test
    void getTrendingPostsDefaultsLimitToTen() throws Exception {
        when(postService.getTrendingPosts(10)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/posts/trending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getViewCountReturnsCount() throws Exception {
        when(postService.getViewCount(7)).thenReturn(new PostViewCountDto(15));

        mockMvc.perform(get("/api/v1/posts/7/views"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewCount").value(15));
    }

    @Test
    void fetchByTagReturnsResponse() throws Exception {
        PostResponse response = new PostResponse();
        when(postService.findPostsByTag(eq("cloud"), anyInt(), anyInt(), anyString(), anyBoolean())).thenReturn(response);

        mockMvc.perform(get("/api/v1/posts/tag/cloud"))
                .andExpect(status().isOk());
    }

    @Test
    void searchPostsReturnsResponse() throws Exception {
        PostResponse response = new PostResponse();
        when(postService.searchPost(eq("java"), anyInt(), anyInt(), anyString(), anyBoolean())).thenReturn(response);

        mockMvc.perform(get("/api/v1/posts/search/java"))
                .andExpect(status().isOk());
    }
}
