package com.trendythread.app.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * OAuth2 login (Google/GitHub) client registrations - only active when
 * {@code oauth2.enabled=true} (see application.yml). This bean simply doesn't
 * exist in the application context otherwise, so {@code SecurityConfig}'s
 * {@code ObjectProvider<ClientRegistrationRepository>} lookup cleanly returns
 * null and OAuth2 login is never wired into the filter chain - the rest of the
 * app is completely unaffected when this feature is disabled (the default).
 */
@Configuration
@ConditionalOnProperty(name = "oauth2.enabled", havingValue = "true")
public class OAuth2ClientRegistrationConfig {

    @Value("${oauth2.google.client-id:}")
    private String googleClientId;

    @Value("${oauth2.google.client-secret:}")
    private String googleClientSecret;

    @Value("${oauth2.github.client-id:}")
    private String githubClientId;

    @Value("${oauth2.github.client-secret:}")
    private String githubClientSecret;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> registrations = new ArrayList<>();

        if (googleClientId != null && !googleClientId.isBlank()) {
            registrations.add(googleBuilder("google")
                    .clientId(googleClientId)
                    .clientSecret(googleClientSecret)
                    .scope("openid", "profile", "email")
                    .build());
        }

        if (githubClientId != null && !githubClientId.isBlank()) {
            registrations.add(githubBuilder("github")
                    .clientId(githubClientId)
                    .clientSecret(githubClientSecret)
                    .scope("read:user", "user:email")
                    .build());
        }

        return new InMemoryClientRegistrationRepository(registrations);
    }

    // Spring Security's CommonOAuth2Provider was removed in 6.5 - these builders
    // replicate its GOOGLE/GITHUB defaults by hand since they're no longer provided.
    private static ClientRegistration.Builder googleBuilder(String registrationId) {
        return ClientRegistration.withRegistrationId(registrationId)
                .clientName("Google")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .issuerUri("https://accounts.google.com");
    }

    private static ClientRegistration.Builder githubBuilder(String registrationId) {
        return ClientRegistration.withRegistrationId(registrationId)
                .clientName("GitHub")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri("https://api.github.com/user")
                .userNameAttributeName("id");
    }
}
