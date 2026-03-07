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
     * The actual refresh token string (should be hashed in production).
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
