package com.trendythread.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trendythread.app.dto.BloggerDto;
import com.trendythread.app.payloads.JwtTokenResponse;
import com.trendythread.app.payloads.LoginRequest;
import com.trendythread.app.payloads.RefreshTokenRequest;
import com.trendythread.app.services.BloggersService;
import com.trendythread.app.services.EmailService;
import com.trendythread.app.services.LoginAttemptService;
import com.trendythread.app.services.RefreshTokenService;
import com.trendythread.app.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private BloggersService bloggersService;

    @Mock
    private EmailService emailService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    private BloggerDto blogger(String email) {
        BloggerDto dto = new BloggerDto();
        dto.setId(2);
        dto.setUserName("johndoe");
        dto.setEmail(email);
        dto.setRole("ROLE_USER");
        return dto;
    }

    private JwtTokenResponse accessTokenResponse(String email) {
        return JwtTokenResponse.builder()
                .token("access-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .username(email)
                .build();
    }

    @Test
    void loginWithValidCredentialsReturnsTokensAndUserDetails() throws Exception {
        String email = "john.doe@example.com";
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword("P@ssword#123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new TestingAuthenticationToken(email, "pw", List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        when(bloggersService.findByEmail(email)).thenReturn(blogger(email));
        when(jwtUtil.generateToken(eq(email), any())).thenReturn(accessTokenResponse(email));
        when(refreshTokenService.issue(email)).thenReturn("raw-refresh-token");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("raw-refresh-token"))
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));

        verify(loginAttemptService).recordSuccess(email);
    }

    @Test
    void loginWithBadCredentialsReturns401() throws Exception {
        String email = "john.doe@example.com";
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad creds"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));

        verify(loginAttemptService).recordFailure(email);
    }

    @Test
    void loginBlockedByRateLimiterReturns429() throws Exception {
        String email = "john.doe@example.com";
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword("P@ssword#123");

        when(loginAttemptService.isBlocked(email)).thenReturn(true);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("Too many failed login attempts. Please try again later."));

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void loginWithMissingPasswordReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"john.doe@example.com\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logoutRevokesRefreshTokensForUser() throws Exception {
        when(jwtUtil.extractUsername("valid-access-token")).thenReturn("john.doe@example.com");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer valid-access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john.doe@example.com"));

        verify(refreshTokenService).revokeAllForUser("john.doe@example.com");
    }

    @Test
    void logoutWithoutAuthorizationHeaderReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isBadRequest());

        verify(refreshTokenService, never()).revokeAllForUser(anyString());
    }

    @Test
    void refreshWithValidTokenReturnsRotatedTokenPair() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("old-refresh-token");

        JwtTokenResponse rotated = accessTokenResponse("john.doe@example.com");
        rotated.setRefreshToken("new-refresh-token");
        when(refreshTokenService.refresh("old-refresh-token")).thenReturn(rotated);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    void refreshWithMissingTokenReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshWithInvalidOrReusedTokenReturns401() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("already-rotated-token");

        when(refreshTokenService.refresh("already-rotated-token"))
                .thenThrow(new IllegalArgumentException("Invalid refresh token"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid refresh token"));
    }

    @Test
    void refreshWithExpiredTokenReturns401() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("expired-token");

        when(refreshTokenService.refresh("expired-token"))
                .thenThrow(new IllegalArgumentException("Refresh token expired"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Refresh token expired"));
    }
}
