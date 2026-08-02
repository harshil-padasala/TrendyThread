package com.trendythread.app.config.security;

import com.trendythread.app.filter.JwtSecurityFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity // ADD THIS LINE - Enables @PreAuthorize, @Secured, @RolesAllowed
@EnableWebSecurity
public class SecurityConfig {

    public final JwtSecurityFilter jwtSecurityFilter;

    @Autowired
    public SecurityConfig(JwtSecurityFilter jwtSecurityFilter) {
        this.jwtSecurityFilter = jwtSecurityFilter;
    }

    @Bean
    public PasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain httpSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // Auth endpoints - public
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/signup", "/api/v1/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").authenticated()

                        // Category READ endpoints - public (fixed: category not categories)
                        .requestMatchers(HttpMethod.GET, "/api/v1/category/**").permitAll()

                        // Tag READ endpoints - public
                        .requestMatchers(HttpMethod.GET, "/api/v1/tags/**").permitAll()

                        // Admin category management - ADMIN role required
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // Category management (POST, PUT, DELETE) - ADMIN role required
                        .requestMatchers(HttpMethod.POST, "/api/v1/category").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/category/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/category/**").hasRole("ADMIN")

                        // Post management (CREATE, UPDATE, DELETE) - authenticated users
                        .requestMatchers(HttpMethod.POST, "/api/v1/posts").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/posts/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/posts/**").authenticated()

                        // Comment management - authenticated users
                        .requestMatchers(HttpMethod.POST, "/api/v1/posts/*/comments").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/posts/*/comments/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/posts/*/comments/**").authenticated()

                        // Blogger profile - authenticated users
                        .requestMatchers(HttpMethod.GET, "/api/v1/bloggers/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/bloggers/me").authenticated()

                        // Swagger/API docs - public
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()

                        // H2 Console - public (development only)
                        .requestMatchers("/h2-console/**").permitAll()

                        // Posts READ endpoints - public
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/**").permitAll()

                        // Post Comment READ endpoints - public
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/*/comments/**").permitAll()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtSecurityFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Unauthorized - " + authException.getMessage() + "\"}");
                        })
                        .accessDeniedHandler(((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Access Denied - " + accessDeniedException.getMessage() + "\"}");
                        }))
                );

        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
        return roleHierarchy;
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        return expressionHandler;
    }
}

