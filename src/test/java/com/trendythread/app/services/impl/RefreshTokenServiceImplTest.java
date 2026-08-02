package com.trendythread.app.services.impl;

import com.trendythread.app.dto.BloggerDto;
import com.trendythread.app.entities.RefreshToken;
import com.trendythread.app.payloads.JwtTokenResponse;
import com.trendythread.app.repositories.RefreshTokenRepository;
import com.trendythread.app.services.BloggersService;
import com.trendythread.app.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private BloggersService bloggersService;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private static final String USERNAME = "john.doe@example.com";

    private BloggerDto blogger() {
        BloggerDto dto = new BloggerDto();
        dto.setId(2);
        dto.setUserName("johndoe");
        dto.setEmail(USERNAME);
        dto.setRole("ROLE_USER");
        return dto;
    }

    @Test
    void issuePersistsHashedTokenNotRawToken() {
        when(jwtUtil.generateRefreshToken(USERNAME)).thenReturn("raw-refresh-token");
        when(jwtUtil.getRefreshValidityMs()).thenReturn(604800000L);

        String raw = refreshTokenService.issue(USERNAME);

        assertEquals("raw-refresh-token", raw);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken saved = captor.getValue();

        assertEquals(USERNAME, saved.getUsername());
        assertNotEquals("raw-refresh-token", saved.getToken());
        assertEquals(64, saved.getToken().length()); // SHA-256 hex digest length
    }

    @Test
    void refreshRotatesValidTokenAndDeletesOldRow() {
        String rawOldToken = "old-raw-refresh-token";
        String hashedOldToken = sha256Hex(rawOldToken);

        RefreshToken stored = RefreshToken.builder()
                .id(1L)
                .token(hashedOldToken)
                .username(USERNAME)
                .createdAt(Instant.now().minusSeconds(60))
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(jwtUtil.extractUsername(rawOldToken)).thenReturn(USERNAME);
        when(jwtUtil.isTokenExpired(rawOldToken)).thenReturn(false);
        when(refreshTokenRepository.findByToken(hashedOldToken)).thenReturn(Optional.of(stored));
        when(bloggersService.findByEmail(USERNAME)).thenReturn(blogger());
        when(jwtUtil.generateToken(eq(USERNAME), any())).thenReturn(
                JwtTokenResponse.builder().token("new-access-token").username(USERNAME).build());
        when(jwtUtil.generateRefreshToken(USERNAME)).thenReturn("new-raw-refresh-token");
        when(jwtUtil.getRefreshValidityMs()).thenReturn(604800000L);

        JwtTokenResponse response = refreshTokenService.refresh(rawOldToken);

        assertEquals("new-access-token", response.getToken());
        assertEquals("new-raw-refresh-token", response.getRefreshToken());
        assertEquals(2, response.getUserId());
        assertEquals("ROLE_USER", response.getRole());

        verify(refreshTokenRepository).delete(stored);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void refreshRejectsTokenNotFoundInDb() {
        when(jwtUtil.extractUsername(anyString())).thenReturn(USERNAME);
        when(jwtUtil.isTokenExpired(anyString())).thenReturn(false);
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> refreshTokenService.refresh("unknown-token"));
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void refreshRejectsExpiredJwt() {
        when(jwtUtil.extractUsername(anyString())).thenReturn(USERNAME);
        when(jwtUtil.isTokenExpired(anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> refreshTokenService.refresh("expired-jwt"));
        verify(refreshTokenRepository, never()).findByToken(anyString());
    }

    @Test
    void refreshRejectsAndDeletesDbExpiredRow() {
        String rawToken = "raw-token";
        String hashed = sha256Hex(rawToken);

        RefreshToken expiredRow = RefreshToken.builder()
                .id(1L)
                .token(hashed)
                .username(USERNAME)
                .createdAt(Instant.now().minusSeconds(700_000))
                .expiresAt(Instant.now().minusSeconds(1))
                .build();

        when(jwtUtil.extractUsername(rawToken)).thenReturn(USERNAME);
        when(jwtUtil.isTokenExpired(rawToken)).thenReturn(false);
        when(refreshTokenRepository.findByToken(hashed)).thenReturn(Optional.of(expiredRow));

        assertThrows(IllegalArgumentException.class, () -> refreshTokenService.refresh(rawToken));
        verify(refreshTokenRepository).delete(expiredRow);
    }

    @Test
    void refreshRejectsMalformedToken() {
        when(jwtUtil.extractUsername(anyString())).thenThrow(new RuntimeException("malformed JWT"));

        assertThrows(IllegalArgumentException.class, () -> refreshTokenService.refresh("not-a-jwt"));
        verify(refreshTokenRepository, never()).findByToken(anyString());
    }

    @Test
    void revokeAllForUserDelegatesToRepository() {
        refreshTokenService.revokeAllForUser(USERNAME);
        verify(refreshTokenRepository).deleteByUsername(USERNAME);
    }

    private static String sha256Hex(String raw) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
