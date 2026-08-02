package com.trendythread.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trendythread.app.dto.BloggerDto;
import com.trendythread.app.dto.UpdateProfileDto;
import com.trendythread.app.services.BloggersService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BloggersControllerTest {

    @Mock
    private BloggersService bloggersService;

    @InjectMocks
    private BloggersController bloggersController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Principal PRINCIPAL = () -> "john.doe@example.com";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bloggersController).build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private BloggerDto blogger() {
        BloggerDto dto = new BloggerDto();
        dto.setId(2);
        dto.setUserName("johndoe");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john.doe@example.com");
        dto.setPassword("P@ssword#123");
        dto.setRole("ROLE_USER");
        return dto;
    }

    @Test
    void fetchCurrentUserProfileReturnsAuthenticatedUser() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("john.doe@example.com", "pw"));
        when(bloggersService.findByEmail("john.doe@example.com")).thenReturn(blogger());

        mockMvc.perform(get("/api/v1/bloggers/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void fetchCurrentUserProfileReturns404WhenUserMissing() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("ghost@example.com", "pw"));
        when(bloggersService.findByEmail("ghost@example.com")).thenReturn(null);

        mockMvc.perform(get("/api/v1/bloggers/me"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCurrentUserProfileReturnsUpdatedProfile() throws Exception {
        UpdateProfileDto request = new UpdateProfileDto("Johnny", "Doe", "Updated bio");
        BloggerDto updated = blogger();
        updated.setFirstName("Johnny");
        when(bloggersService.updateCurrentUserProfile(any(UpdateProfileDto.class), eq("john.doe@example.com")))
                .thenReturn(updated);

        mockMvc.perform(put("/api/v1/bloggers/me")
                        .principal(PRINCIPAL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Johnny"));
    }

    @Test
    void fetchByBloggerIdReturnsBlogger() throws Exception {
        when(bloggersService.findByBloggerId(2)).thenReturn(blogger());

        mockMvc.perform(get("/api/v1/bloggers/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void fetchAllBloggersReturnsList() throws Exception {
        when(bloggersService.fetchAllBloggers()).thenReturn(List.of(blogger()));

        mockMvc.perform(get("/api/v1/bloggers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updateByBloggerIdReturnsUpdatedBlogger() throws Exception {
        BloggerDto request = blogger();
        BloggerDto updated = blogger();
        updated.setUserName("newname");
        when(bloggersService.updateByBloggerId(any(BloggerDto.class), eq(2))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/bloggers/2")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("newname"));
    }

    @Test
    void deleteByBloggerIdReturnsSuccessResponse() throws Exception {
        mockMvc.perform(delete("/api/v1/bloggers/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(bloggersService).deleteByBloggerId(2);
    }
}
