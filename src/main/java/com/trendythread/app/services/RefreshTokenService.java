package com.trendythread.app.services;

import com.trendythread.app.payloads.JwtTokenResponse;
import org.springframework.stereotype.Service;

@Service
public interface RefreshTokenService {

    /**
     * Generate a refresh token for the given username, persist its hash, and
     * return the raw token string to hand back to the client.
     */
    String issue(String username);

    /**
     * Validate the given raw refresh token, rotate it (invalidate the old one,
     * issue a new access + refresh token pair), and return the new pair.
     *
     * @throws IllegalArgumentException if the token is invalid, unknown, or expired
     */
    JwtTokenResponse refresh(String rawRefreshToken);

    /**
     * Revoke all refresh tokens belonging to a user (used on logout).
     */
    void revokeAllForUser(String username);
}
