package com.trendythread.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trendythread.app.dto.CommentDto;
import com.trendythread.app.exceptions.GlobalExceptionHandler;
import com.trendythread.app.exceptions.ResourceNotFoundException;
import com.trendythread.app.services.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Principal PRINCIPAL = () -> "john.doe@example.com";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private CommentDto validCommentDto() {
        CommentDto dto = new CommentDto();
        dto.setName("John Doe");
        dto.setEmail("john.doe@example.com");
        dto.setContent("This is a sufficiently long comment.");
        return dto;
    }

    @Test
    void createCommentReturns201() throws Exception {
        CommentDto request = validCommentDto();
        CommentDto saved = validCommentDto();
        saved.setId(1);
        when(commentService.createComment(any(CommentDto.class), eq(5), eq("john.doe@example.com"))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/post/5/comments")
                        .principal(PRINCIPAL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createCommentWithParentIdPassesThroughToService() throws Exception {
        CommentDto request = validCommentDto();
        request.setParentId(3);
        CommentDto saved = validCommentDto();
        saved.setId(4);
        saved.setParentId(3);
        when(commentService.createComment(any(CommentDto.class), eq(5), eq("john.doe@example.com"))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/post/5/comments")
                        .principal(PRINCIPAL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.parentId").value(3));
    }

    @Test
    void findByPostIdReturnsCommentList() throws Exception {
        when(commentService.findByPostId(5)).thenReturn(List.of(validCommentDto()));

        mockMvc.perform(get("/api/v1/posts/5/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void fetchByPostIdAndCommentIdReturnsComment() throws Exception {
        CommentDto comment = validCommentDto();
        comment.setId(9);
        when(commentService.findByPostIdAndCommentId(5, 9)).thenReturn(comment);

        mockMvc.perform(get("/api/v1/posts/5/comments/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9));
    }

    @Test
    void fetchByPostIdAndCommentIdNotFoundReturns404() throws Exception {
        when(commentService.findByPostIdAndCommentId(5, 999))
                .thenThrow(new ResourceNotFoundException("Comment", "commentId", 999));

        mockMvc.perform(get("/api/v1/posts/5/comments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateByPostIdAndCommentIdReturnsUpdatedComment() throws Exception {
        CommentDto request = validCommentDto();
        CommentDto updated = validCommentDto();
        updated.setId(9);
        when(commentService.updateByPostIdAndCommentId(eq(5), eq(9), any(CommentDto.class), eq("john.doe@example.com")))
                .thenReturn(updated);

        mockMvc.perform(put("/api/v1/posts/5/comments/9")
                        .principal(PRINCIPAL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9));
    }

    @Test
    void deleteCommentReturnsSuccessResponse() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/5/comments/9"))
                .andExpect(jsonPath("$.success").value(true));

        verify(commentService).deleteByCommentId(9);
    }
}
