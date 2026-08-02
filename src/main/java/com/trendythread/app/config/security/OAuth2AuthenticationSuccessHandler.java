package com.trendythread.app.config.security;

import com.trendythread.app.entities.Blogger;
import com.trendythread.app.payloads.JwtTokenResponse;
import com.trendythread.app.repositories.BloggersRepository;
import com.trendythread.app.services.RefreshTokenService;
import com.trendythread.app.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * On successful OAuth2 login (Google/GitHub), find-or-create a Blogger by
 * email and issue our own access + refresh token pair - bridging OAuth2 into
 * the existing JWT auth system rather than running two parallel auth models.
 * Redirects back to the SPA with the tokens as query params for it to pick up
 * (see /oauth2/callback on the frontend).
 */
@Component
@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private BloggersRepository bloggersRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String provider = authentication instanceof OAuth2AuthenticationToken token
                ? token.getAuthorizedClientRegistrationId()
                : "oauth2";

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null || email.isBlank()) {
            // GitHub can omit email even with the user:email scope if the user keeps it private.
            String login = oAuth2User.getAttribute("login");
            email = (login != null ? login : "user" + System.nanoTime()) + "@users.noreply." + provider + ".local";
        }
        if (name == null || name.isBlank()) {
            name = email.split("@")[0];
        }

        final String finalEmail = email;
        final String finalName = name;
        Blogger blogger = bloggersRepository.findByEmail(email)
                .orElseGet(() -> createOAuthBlogger(finalEmail, finalName));

        log.info("OAuth2 login successful via {} for email={}", provider, email);

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", List.of(blogger.getRole()));
        extraClaims.put("email", blogger.getEmail());

        JwtTokenResponse tokenResponse = jwtUtil.generateToken(blogger.getEmail(), extraClaims);
        String refreshToken = refreshTokenService.issue(blogger.getEmail());

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/callback")
                .queryParam("token", tokenResponse.getToken())
                .queryParam("refreshToken", refreshToken)
                .queryParam("userId", blogger.getId())
                .queryParam("name", blogger.getUserName())
                .queryParam("role", blogger.getRole())
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    /** New OAuth2 users get a random, unusable password - they only ever log in via OAuth. */
    private Blogger createOAuthBlogger(String email, String displayName) {
        String[] parts = displayName.trim().split("\\s+", 2);

        Blogger blogger = new Blogger();
        blogger.setEmail(email);
        blogger.setUserName(sanitizeUsername(displayName) + "-" + UUID.randomUUID().toString().substring(0, 6));
        blogger.setFirstName(parts[0].isBlank() ? "OAuth" : parts[0]);
        blogger.setLastName(parts.length > 1 ? parts[1] : "User");
        blogger.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        blogger.setRole("ROLE_USER");

        Blogger saved = bloggersRepository.save(blogger);
        log.info("Created new blogger from OAuth2 login: id={}, email={}", saved.getId(), email);
        return saved;
    }

    private String sanitizeUsername(String raw) {
        String cleaned = raw.replaceAll("[^a-zA-Z0-9]", "");
        return cleaned.isBlank() ? "user" : cleaned;
    }
}
