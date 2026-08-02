package com.trendythread.app.services.impl;

import com.trendythread.app.dto.BloggerDto;
import com.trendythread.app.entities.RefreshToken;
import com.trendythread.app.payloads.JwtTokenResponse;
import com.trendythread.app.repositories.RefreshTokenRepository;
import com.trendythread.app.services.BloggersService;
import com.trendythread.app.services.RefreshTokenService;
import com.trendythread.app.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BloggersService bloggersService;

    @Override
    public String issue(String username) {
        String rawToken = jwtUtil.generateRefreshToken(username);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(hash(rawToken))
                .username(username)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(jwtUtil.getRefreshValidityMs()))
                .build();

        refreshTokenRepository.save(refreshTokenEntity);
        return rawToken;
    }

    @Override
    public JwtTokenResponse refresh(String rawRefreshToken) {
        String username;
        try {
            username = jwtUtil.extractUsername(rawRefreshToken);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        if (username == null || jwtUtil.isTokenExpired(rawRefreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String tokenHash = hash(rawRefreshToken);
        Optional<RefreshToken> stored = refreshTokenRepository.findByToken(tokenHash);
        if (stored.isEmpty()) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        RefreshToken refreshTokenEntity = stored.get();
        if (refreshTokenEntity.isExpired()) {
            refreshTokenRepository.delete(refreshTokenEntity);
            throw new IllegalArgumentException("Refresh token expired");
        }

        if (!refreshTokenEntity.getUsername().equals(username)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // Rotation: the old refresh token is single-use.
        refreshTokenRepository.delete(refreshTokenEntity);

        BloggerDto user = bloggersService.findByEmail(username);
        if (user == null) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        List<String> roles = user.getRole() != null ? List.of(user.getRole()) : List.of();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", roles);
        extraClaims.put("email", user.getEmail());

        JwtTokenResponse tokenResponse = jwtUtil.generateToken(username, extraClaims);

        String newRefreshToken = issue(username);
        tokenResponse.setRefreshToken(newRefreshToken);
        tokenResponse.setRoles(roles);
        tokenResponse.setUserId(user.getId());
        tokenResponse.setName(user.getUserName());
        tokenResponse.setRole(user.getRole());

        log.debug("Rotated refresh token for user: {}", username);
        return tokenResponse;
    }

    @Override
    public void revokeAllForUser(String username) {
        refreshTokenRepository.deleteByUsername(username);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
