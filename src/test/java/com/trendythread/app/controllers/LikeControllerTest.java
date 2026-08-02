package com.trendythread.app.controllers;

import com.trendythread.app.dto.LikeStatusDto;
import com.trendythread.app.services.LikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LikeControllerTest {

    @Mock
    private LikeService likeService;

    @InjectMocks
    private LikeController likeController;

    private MockMvc mockMvc;
    private static final Principal PRINCIPAL = () -> "john.doe@example.com";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(likeController).build();
    }

    @Test
    void toggleLikeReturnsUpdatedStatus() throws Exception {
        when(likeService.toggleLike(5, "john.doe@example.com")).thenReturn(new LikeStatusDto(true, 1));

        mockMvc.perform(post("/api/v1/posts/5/like").principal(PRINCIPAL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.likeCount").value(1));
    }

    @Test
    void getLikeStatusForAnonymousRequestPassesNullPrincipal() throws Exception {
        when(likeService.getLikeStatus(5, null)).thenReturn(new LikeStatusDto(false, 4));

        mockMvc.perform(get("/api/v1/posts/5/like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(false))
                .andExpect(jsonPath("$.likeCount").value(4));
    }

    @Test
    void getLikeStatusForAuthenticatedRequestUsesPrincipalEmail() throws Exception {
        when(likeService.getLikeStatus(5, "john.doe@example.com")).thenReturn(new LikeStatusDto(true, 4));

        mockMvc.perform(get("/api/v1/posts/5/like").principal(PRINCIPAL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true));
    }
}
