package com.trendythread.app.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

/**
 * RefreshToken entity to store refresh tokens for logout functionality.
 * When a user logs out, their refresh token is deleted/invalidated,
 * preventing token refresh even if the access token is still valid.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * SHA-256 hex hash of the raw refresh token string. The raw token is only
     * ever handed to the client; only its hash is persisted here so a DB leak
     * alone can't be replayed as a valid refresh token.
     */
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    /**
     * The user ID or username this refresh token belongs to.
     */
    @Column(nullable = false)
    private String username;

    /**
     * Timestamp when this refresh token expires.
     */
    @Column(nullable = false)
    private Instant expiresAt;

    /**
     * Timestamp when token was created.
     */
    @Column(nullable = false)
    private Instant createdAt;

    /**
     * Check if token is expired.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
