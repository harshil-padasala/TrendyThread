package com.trendythread.app.payloads;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

/**
 * JwtTokenResponse is the standard DTO returned by authentication endpoints after
 * successful creation of a JSON Web Token (JWT). This class provides a clear,
 * documented structure for clients to consume authentication information and
 * is annotated with OpenAPI/Swagger {@link io.swagger.v3.oas.annotations.media.Schema}
 * metadata so it appears in generated API documentation.
 *
 * <p>Fields contained in this response cover the typical data needed by clients:
 * - access token (JWT) used for authenticating subsequent requests,
 * - token type (usually "Bearer") used in the Authorization header scheme,
 * - optional refresh token (if your application issues long-lived refresh tokens),
 * - issuedAt and expiresAt timestamps for client-side expiration handling,
 * - expiresIn which provides the time-to-live in seconds,
 * - the username and any granted roles/authorities for convenience.</p>
 *
 * <p>Example JSON payload produced from this DTO:
 * <pre>
 * {
 *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "tokenType": "Bearer",
 *   "refreshToken": "def50200b4...",
 *   "expiresIn": 3600,
 *   "issuedAt": "2026-03-03T10:15:30Z",
 *   "expiresAt": "2026-03-03T11:15:30Z",
 *   "username": "alice@example.com",
 *   "roles": ["ROLE_USER", "ROLE_ADMIN"]
 * }
 * </pre>
 *
 * Usage notes:
 * - Clients should store the access token securely (in memory or secure storage)
 *   and include the header `Authorization: Bearer &lt;token&gt;` on protected requests.
 * - Use expiresAt or expiresIn to refresh or reauthenticate before the token expires.
 * - If a refresh token is present, it should be sent only to the token refresh endpoint
 *   and never exposed to third parties.
 * <p>
 * Security reminder: never log tokens in production logs. Treat tokens like passwords.
 */
@Schema(
        name = "JwtTokenResponse",
        description = "Response object returned after successful authentication containing JWT and related metadata."
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtTokenResponse {

    @Schema(
            description = "The JWT access token. This token must be presented in the Authorization header to access protected endpoints.",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String token;

    @Schema(
            description = "The token type (authorization scheme). Typically 'Bearer'.",
            example = "Bearer"
    )
    private String tokenType = "Bearer";

    @Schema(
            description = "Optional refresh token used to obtain new access tokens without re-authenticating the user. Only present if your API issues refresh tokens.",
            example = "def50200b4..."
    )
    private String refreshToken;

    @Schema(
            description = "Time-to-live (TTL) of the access token in seconds. Clients can use this to schedule token refresh before expiration.",
            example = "3600"
    )
    private Long expiresIn;

    @Schema(
            description = "Timestamp when the token was issued, in ISO-8601 UTC format.",
            example = "2026-03-03T10:15:30Z"
    )
    private Instant issuedAt;

    @Schema(
            description = "Timestamp when the token will expire, in ISO-8601 UTC format.",
            example = "2026-03-03T11:15:30Z"
    )
    private Instant expiresAt;

    @Schema(
            description = "The principal/username for whom the token was issued.",
            example = "alice@example.com"
    )
    private String username;

    @Schema(
            description = "A list of granted authorities/roles associated with the authenticated user. Useful for client-side UI decisions.",
            example = "[\"ROLE_USER\", \"ROLE_ADMIN\"]"
    )
    private List<String> roles;

}
