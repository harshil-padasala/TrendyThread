package com.trendythread.app.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.trendythread.app.payloads.JwtTokenResponse;

/**
 * JwtUtil
 * ------------------------------------------------------------------
 * A utility component for creating and validating JSON Web Tokens (JWTs).
 * <p>
 * This class is intentionally documented extensively to explain the security
 * considerations, how tokens are built and parsed, and recommended best
 * practices when using JWTs in a Spring Boot application.
 * <p>
 * Key responsibilities implemented here:
 * - generateToken(username, extraClaims): build a signed JWT with subject,
 *   issued-at and expiration claims and optional custom claims.
 * - extractAllClaims(token): parse and return all claims from a token.
 * - extractUsername(token): convenience method to get the subject (username).
 * - isTokenValid(token, username): validate token signature, subject and expiration.
 * <p>
 * Security notes & recommendations (high-level):
 * 1) Do NOT hardcode secrets in source code. Use externalized configuration
 *    (environment variables, secrets manager, or application properties) and
 *    make sure production secrets are rotated regularly.
 * 2) Use a sufficiently long secret for HMAC (at least 256 bits for HS256).
 * 3) Prefer asymmetric algorithms (RS256/ES256) for higher security when
 *    token issuance and verification are performed by different parties.
 * 4) Keep token lifetimes short (e.g., minutes to hours) and use refresh tokens
 *    for renewing access without forcing the user to re-authenticate often.
 * 5) Validate subject and other critical claims, and reject tokens with
 *    unexpected claim values.
 * 6) Consider token revocation strategies (blacklist, token versioning in DB,
 *    short-lived tokens) because JWTs are stateless by default.
 * <p>
 * Implementation details:
 * - Uses io.jsonwebtoken (jjwt) to build and parse JWTs.
 * - Reads `jwt.secret` and `jwt.expiration-ms` from application properties.
 *   If the secret is Base64-encoded (recommended), this class decodes it and
 *   constructs an HMAC-SHA key via Keys.hmacShaKeyFor(...).
 * - The class provides extensible hooks: you can pass additional claims when
 *   creating the token (roles, tenant id, etc.). Keep the claims small to
 *   avoid oversized tokens.
 */
@Component
public class JwtUtil {

    /**
     * The secret key used to sign tokens. THIS VALUE SHOULD BE EXTERNALIZED and
     * kept out of source control. Two common formats are supported:
     *  - raw text secret (a long passphrase), or
     *  - Base64-encoded binary secret (recommended because it can represent the
     *    full length bytes required by modern HMAC algorithms).
     * <p>
     * Example application.yml:
     * jwt:
     *   secret: ${JWT_SECRET:change_me_and_store_safely}
     *   expiration-ms: 36000000
     */
    @Value("${jwt.secret:WnI4QExtMiNReDkhdlQ2JE5jNF5IcDcmS3MzKkR3MSVGeTVASnU4IVJiMiNYZTYkTWc5XlB0NCZWYTcqTGMw}")
    private String secret;

    /**
     * Token validity window in milliseconds. Keep this as small as practical.
     * For access tokens, consider values from a few minutes to a few hours.
     */
    @Value("${jwt.expiration-ms:36000000}")
    private long validityInMs; // default 10 hours

    // Signing key derived from the configured secret. Initialized on startup.
    private Key signingKey;

    @PostConstruct
    private void init() {
        // The secret may be provided as Base64-encoded string or as raw characters.
        // If the string looks like Base64 (contains padding or only Base64 chars),
        // we attempt to decode it. If decoding fails, fall back to raw bytes of the
        // UTF-8 string. Using raw UTF-8 bytes is acceptable if the secret was
        // originally generated as text; however, for full entropy, prefer Base64.
        try {
            // Try interpreting secret as Base64 first. If it's not valid Base64,
            // this will throw an IllegalArgumentException and fallback will be used.
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            signingKey = Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException ex) {
            // Fallback: use UTF-8 bytes of the configured secret string.
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            signingKey = Keys.hmacShaKeyFor(keyBytes);
        }
    }

    /**
     * Create a JWT token for the given username with optional extra claims.
     * Now returns a {@link JwtTokenResponse} containing the compact token and
     * useful metadata (issuedAt, expiresAt, expiresIn, etc.).
     *
     * @param username   the subject of the token (typically the username or user id)
     * @param extraClaims a map of additional claims to include (roles, scopes, etc.)
     * @return a JwtTokenResponse containing the signed JWT and metadata
     */
    public JwtTokenResponse generateToken(String username, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMs);

        String compactToken = Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        // Build roles list if present in extraClaims (common practice to include roles as a claim)
        List<String> roles = null;
        if (extraClaims != null && extraClaims.containsKey("roles")) {
            try {
                Object r = extraClaims.get("roles");
                if (r instanceof List) {
                    //noinspection unchecked
                    roles = (List<String>) r;
                } else if (r instanceof String) {
                    roles = List.of((String) r);
                }
            } catch (Exception ignored) {
                // If roles cannot be parsed, leave as null; clients should rely on server-side checks.
            }
        }

        return JwtTokenResponse.builder()
                .token(compactToken)
                .tokenType("Bearer")
                .refreshToken(null)
                .expiresIn(validityInMs / 1000)
                .issuedAt(now.toInstant())
                .expiresAt(expiry.toInstant())
                .username(username)
                .roles(roles)
                .build();
    }

    /**
     * Convenience overload to create a token without extra claims.
     *
     * @param username the subject
     * @return JwtTokenResponse containing signed JWT and metadata
     */
    public JwtTokenResponse generateToken(String username) {
        return generateToken(username, Map.of());
    }

    /**
     * Parse all claims contained in the token.
     *
     * @param token the compact JWT string
     * @return the claims body
     * @throws io.jsonwebtoken.JwtException (or subclass) when token is invalid or malformed
     */
    public Claims extractAllClaims(String token) {
        // parseClaimsJws validates the signature and if valid, returns the Jws<Claims>.
        Jws<Claims> parsed = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);

        return parsed.getBody();
    }

    /**
     * Extract the username (subject) from the token. Returns null or throws
     * an exception depending on the parsing outcome.
     *
     * @param token compact JWT
     * @return subject (username)
     */
    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    /**
     * Validate token for an expected username (subject) and expiration.
     * <p>
     * This method verifies:
     *  - signature is valid
     *  - subject matches the expected username
     *  - token is not expired
     * <p>
     * Note: In addition to these checks, production code should verify any other
     *
     * @param token    compact JWT
     * @param username expected subject
     * @return true if token is valid, false otherwise
     */
    public boolean isTokenValid(String token, String username) {
        try {
            final String tokenUsername = extractUsername(token);
            final Date expiration = extractAllClaims(token).getExpiration();

            if (tokenUsername == null) {
                return false;
            }

            boolean match = tokenUsername.equals(username);
            boolean notExpired = (expiration == null) || expiration.after(new Date());

            if (!match) {
                // Uncomment for debugging: log.debug("JwtUtil - token username '{}' does not match expected username '{}'", tokenUsername, username);
                return false;
            }

            // Uncomment for debugging: log.debug("JwtUtil - token has expired at {}", expiration);
            return notExpired;
        } catch (ExpiredJwtException eje) {
            // Token has expired
            return false;
        } catch (Exception ex) {
            // Any other parsing/validation failure
            return false;
        }
    }

    /**
     * Simple helper to check expiration without other validations. Useful for
     * token-introspection endpoints or refresh flows.
     *
     * @param token JWT string
     * @return true if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractAllClaims(token).getExpiration();
            return expiration != null && expiration.before(new Date());
        } catch (ExpiredJwtException eje) {
            return true;
        } catch (Exception ex) {
            // If the token is malformed or signature invalid, we consider it "expired/invalid" here.
            return true;
        }
    }

    /**
     * Generate a refresh token for the given username.
     * Refresh tokens have a longer validity (e.g., 7 days) and are used to
     * obtain new access tokens without re-authentication.
     *
     * @param username the user
     * @return refresh token string
     */
    public String generateRefreshToken(String username) {
        Date now = new Date();
        // Refresh token valid for 7 days (adjust as needed)
        Date expiry = new Date(now.getTime() + (7 * 24 * 60 * 60 * 1000));

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

}
