package com.trendythread.app.controllers;

import com.trendythread.app.dto.BloggerDto;
import com.trendythread.app.payloads.LoginRequest;
import com.trendythread.app.entities.RefreshToken;
import com.trendythread.app.payloads.JwtTokenResponse;
import com.trendythread.app.repositories.RefreshTokenRepository;
import com.trendythread.app.services.BloggersService;
import com.trendythread.app.services.EmailService;
import com.trendythread.app.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication endpoints (login, logout, refresh)")
@Slf4j
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BloggersService bloggersService;

    @Autowired
    private EmailService emailService;

    /**
     * User registration/signup endpoint.
     * Creates a new blogger account with email and password.
     * <p>
     * Flow:
     * 1. Validate signup request (email, password, name)
     * 2. Check if email already exists in database
     * 3. Validate password strength (min 8 chars, uppercase, lowercase, digit)
     * 4. Encode password using BCryptPasswordEncoder
     * 5. Create new Blogger entity with default role (ROLE_USER)
     * 6. Save Blogger to database
     * 7. Return success response with user details
     * <p>
     * Security considerations:
     * - Passwords are hashed using BCrypt (never stored as plain text)
     * - Email uniqueness is enforced to prevent duplicate accounts
     * - Password strength validation prevents weak passwords
     * - Default role is ROLE_USER; admins must promote manually via database/admin panel
     * - No JWT token is issued on signup; user must login separately
     *
     * @param bloggerDto contains email, password, and name
     * @return success message with user details or error response
     */
    @PostMapping("/signup")
    @Operation(
            summary = "User Registration",
            description = "Creates a new blogger account with email and password. User must login separately after registration.",
            tags = "Authentication"
    )
    @ApiResponse(
            responseCode = "201",
            description = "User registration successful",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            example = "{\"message\": \"User registered successfully\", \"email\": \"user@example.com\", \"name\": \"John Doe\", \"id\": 1}"
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid input or email already exists"
    )
    @ApiResponse(
            responseCode = "500",
            description = "Server error during registration"
    )
    public ResponseEntity<?> signup(@RequestBody BloggerDto bloggerDto) {
        try {
            // ===== STEP 1: Validate input =====
            if (bloggerDto == null || bloggerDto.getEmail() == null ||
                    bloggerDto.getPassword() == null || bloggerDto.getUserName() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email, password, and name are required"));
            }

            String email = bloggerDto.getEmail().trim();
            String password = bloggerDto.getPassword();
            String name = bloggerDto.getUserName().trim();

            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email, password, and name cannot be empty"));
            }

            log.debug("Signup request received for email: {}", email);

            // ===== STEP 2: Check if email already exists =====
            BloggerDto existingBlogger = bloggersService.findByEmail(email);
            if (existingBlogger != null) {
                log.warn("Signup attempt with existing email: {}", email);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email already registered. Please login or use a different email."));
            }

            // ===== STEP 3: Create new Blogger entity =====
            BloggerDto savedBlogger = bloggersService.createBlogger(bloggerDto);


            emailService.sendWelcomeEmail(savedBlogger);
            log.debug("Sent welcome email to: {}", savedBlogger.getEmail());

            // ===== STEP 4: Return success response =====
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("id", savedBlogger.getId());
            response.put("email", savedBlogger.getEmail());
            response.put("name", savedBlogger.getUserName());
            response.put("role", savedBlogger.getRole() != null ? savedBlogger.getRole() : "ROLE_USER"); // ADD: Include role in signup response
            response.put("note", "Please login with your credentials to receive JWT tokens");

            log.info("User registration successful for email: {}", email);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Signup failed for email: {}",
                    (bloggerDto != null ? bloggerDto.getEmail() : "unknown"), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Registration failed",
                            "details", e.getMessage()
                    ));
        }
    }


    /**
     * User login endpoint.
     * Authenticates user credentials and returns access token + refresh token.
     * <p>
     * Flow:
     * 1. Validate login request (email and password)
     * 2. Authenticate using Spring Security's AuthenticationManager
     * 3. Fetch user details from database
     * 4. Extract user roles/authorities
     * 5. Generate access token (short-lived, e.g., 1 hour)
     * 6. Generate refresh token (long-lived, e.g., 7 days)
     * 7. Save refresh token to database for logout/revocation
     * 8. Return JwtTokenResponse with both tokens
     * <p>
     * Security considerations:
     * - Passwords are validated by Spring Security (never stored in JWT)
     * - Refresh tokens are stored server-side for invalidation on logout
     * - Access tokens are short-lived to minimize damage from token theft
     * - User roles are included in response but should be re-validated server-side
     *
     * @param loginRequest contains email and password
     * @return JwtTokenResponse with access token, refresh token, and metadata
     */
    @PostMapping("/login")
    @Operation(
            summary = "User Login",
            description = "Authenticates user with email and password. Returns access token (short-lived) and refresh token (long-lived).",
            tags = "Authentication"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            example = "{\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"tokenType\": \"Bearer\", \"expiresIn\": 3600, \"username\": \"user@example.com\", \"roles\": [\"ROLE_USER\"]}"
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid email or password"
    )
    @ApiResponse(
            responseCode = "500",
            description = "Server error during authentication"
    )
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // ===== STEP 1: Validate input =====
            if (loginRequest == null || loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email and password are required"));
            }

            String email = loginRequest.getEmail().trim();
            String password = loginRequest.getPassword();

            if (email.isEmpty() || password.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email and password cannot be empty"));
            }
            log.debug("Login request received for email: {}", email);

            // ===== STEP 2: Authenticate using Spring Security =====
            // This delegates to your configured AuthenticationProvider (e.g., DaoAuthenticationProvider)
            // which validates password against the stored user in database.
            Authentication authentication;
            try {
                authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(email, password)
                );
            } catch (BadCredentialsException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid email or password"));
            }
            log.debug("Authentication successful for email: {}", email);

            // ===== STEP 3: Fetch user from database =====
            BloggerDto user = bloggersService.findByEmail(email);

            if (user == null) {
                // This should rarely happen since authentication succeeded, but safety check.
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "User not found in database"));
            }
            log.debug("Fetched user details from database for email: {}", email);

            // ===== STEP 4: Extract roles/authorities =====
            List<String> roles = authentication.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            log.debug("Extracted roles for user {}: {}", email, roles);

            // ===== STEP 5: Generate access token (short-lived) =====
            // Access token is valid for 1 hour (3600 seconds)
            // Include roles in extra claims so they're available in the token
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("roles", roles);
            extraClaims.put("email", user.getEmail());

            JwtTokenResponse tokenResponse = jwtUtil.generateToken(email, extraClaims);
            log.debug("Generated access token for email: {}", email);

            // ===== STEP 6: Generate refresh token (long-lived) =====
            String refreshTokenString = jwtUtil.generateRefreshToken(email);
            log.debug("Generated refresh token for email: {}", email);

            // ===== STEP 7: Save refresh token to database =====
            // Refresh tokens are stored server-side so they can be invalidated on logout
            // Token expiry: 7 days from now
            RefreshToken refreshTokenEntity = RefreshToken.builder()
                    .token(refreshTokenString)
                    .username(email)
                    .createdAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(7 * 24 * 60 * 60)) // 7 days
                    .build();

            refreshTokenRepository.save(refreshTokenEntity);
            log.debug("Saved refresh token to database for email: {}", email);

            // ===== STEP 8: Set user details and refresh token in response and return =====
            tokenResponse.setRefreshToken(refreshTokenString);
            tokenResponse.setRoles(roles);
            tokenResponse.setUserId(user.getId());
            tokenResponse.setName(user.getUserName());
            tokenResponse.setRole(user.getRole()); // ADD: Include user's role for frontend access control
            log.debug("Login successful for email: {}. Returning token response with role: {}", email, user.getRole());

            return ResponseEntity.ok(tokenResponse);

        } catch (Exception e) {
            // Log the exception for debugging (use your logger if available)
            log.error("Login failed for email: {}", Objects.nonNull(loginRequest) ? loginRequest.getEmail() : "", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Authentication failed",
                            "details", e.getMessage()
                    ));
        }
    }

    /**
     * Logout endpoint: invalidate the user's refresh token.
     * After logout, the refresh token cannot be used to generate new access tokens.
     *
     * @param authHeader Authorization header containing Bearer token
     * @return success message or error response
     */
    @Operation(
            summary = "User Logout",
            description = "Invalidates the user's refresh token, preventing further token refreshes",
            security = @SecurityRequirement(name = "Bearer")
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully logged out",
            content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"Logged out successfully\", \"username\": \"user@example.com\"}"))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid or missing Authorization header"
    )
    @ApiResponse(
            responseCode = "500",
            description = "Server error during logout"
    )
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            // Validate header exists
            if (authHeader == null || authHeader.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Authorization header is required"));
            }

            // Extract username from token
            String username = extractUsernameFromHeader(authHeader);
            log.debug("Logout request received for user: {}", username);

            // Delete all refresh tokens for this user
            refreshTokenRepository.deleteByUsername(username);
            log.debug("Deleted refresh tokens for user: {}", username);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Logged out successfully");
            response.put("username", username);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Logout failed: " + e.getMessage()));
        }
    }


    /**
     * Extract username from Authorization header.
     * Expected format: "Bearer <jwt_token>"
     *
     * @param authHeader the Authorization header value
     * @return username extracted from JWT claims
     * @throws IllegalArgumentException if header is invalid or token is malformed
     */
    private String extractUsernameFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }

        // Remove "Bearer " prefix to get the token
        String token = authHeader.substring(7);

        // Extract username from token using JwtUtil
        return jwtUtil.extractUsername(token);
    }

}
