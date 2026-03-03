package com.trendythread.app.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS (Cross-Origin Resource Sharing) Configuration for TrendyThread Blog Application.
 * <p>
 * This configuration class enables and configures CORS support for the REST API,
 * allowing frontend applications running on different origins to make HTTP requests
 * to the backend API. Without proper CORS configuration, browsers enforce the
 * Same-Origin Policy (SOP), which blocks requests from different origins.
 * <p>
 * <b>What is CORS?</b>
 * <p>
 * CORS is a security feature implemented by web browsers that restricts scripts running
 * on one origin from accessing resources on another origin. An origin is defined as:
 * <pre>
 *   Origin = Scheme + Host + Port
 *   Example: https://example.com:8443
 * </pre>
 * Without CORS headers, the browser blocks cross-origin requests automatically.
 * <p>
 * <b>Current Configuration:</b>
 * <p>
 * This configuration allows:
 * <ul>
 *     <li><b>Allowed Origins:</b> http://localhost:3000 (frontend development server)</li>
 *     <li><b>Allowed Methods:</b> GET, POST, PUT, DELETE, OPTIONS</li>
 *     <li><b>Allowed Headers:</b> All headers (*)</li>
 *     <li><b>Credentials:</b> Allowed (cookies, authorization headers)</li>
 *     <li><b>Path Pattern:</b> All endpoints (/**)</li>
 * </ul>
 * <p>
 * <b>How CORS Works:</b>
 * <p>
 * When a browser detects a cross-origin request:
 * <ol>
 *     <li>Browser sends a preflight OPTIONS request with CORS headers</li>
 *     <li>Server responds with CORS headers indicating what's allowed</li>
 *     <li>If allowed, browser sends the actual request (GET, POST, PUT, DELETE)</li>
 *     <li>If not allowed, browser blocks the request</li>
 * </ol>
 * <p>
 * <b>Example Preflight Request:</b>
 * <pre>
 * OPTIONS /api/v1/posts HTTP/1.1
 * Host: localhost:8079
 * Origin: http://localhost:3000
 * Access-Control-Request-Method: POST
 * Access-Control-Request-Headers: Content-Type, Authorization
 * </pre>
 * <p>
 * <b>Example Server Response:</b>
 * <pre>
 * HTTP/1.1 200 OK
 * Access-Control-Allow-Origin: http://localhost:3000
 * Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
 * Access-Control-Allow-Headers: *
 * Access-Control-Allow-Credentials: true
 * Access-Control-Max-Age: 3600
 * </pre>
 * <p>
 * <b>Current Use Cases:</b>
 * <ul>
 *     <li>Frontend React/Angular app at http://localhost:3000 can call backend API at http://localhost:8079</li>
 *     <li>Frontend can include Authorization headers (JWT tokens) in requests</li>
 *     <li>Frontend can use cookies for session management (if implemented)</li>
 * </ul>
 * <p>
 * <b>Security Considerations:</b>
 * <ul>
 *     <li><b>Allowed Origins:</b> Currently allows only http://localhost:3000.
 *       This is appropriate for development. For production, change to your actual
 *       frontend domain (e.g., https://example.com)</li>
 *     <li><b>Wildcard Origins (*):</b> Never use "http://*" or "*" in production
 *       as it allows any website to access your API. This is a security risk.</li>
 *     <li><b>Allowed Methods:</b> Only allow methods your frontend actually uses.
 *       For example, if frontend doesn't need DELETE, don't allow it.</li>
 *     <li><b>Allowed Headers (*):</b> Allows all headers. In production, consider
 *       restricting to only required headers (Content-Type, Authorization, etc.)</li>
 *     <li><b>Credentials (true):</b> Allows credentials (cookies, auth headers).
 *       Required for JWT authentication. Set to false if not using credentials.</li>
 *     <li><b>Max Age:</b> Browser caches preflight response for this duration.
 *       Current: default (3600 seconds). Longer = fewer preflight requests.</li>
 * </ul>
 * <p>
 * <b>Production Configuration Example:</b>
 * <p>
 * For production deployment with domain https://trendy-thread.com:
 * <pre>
 * registry.addMapping("/**")
 *         .allowedOrigins("https://trendy-thread.com")
 *         .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
 *         .allowedHeaders("Content-Type", "Authorization")
 *         .allowCredentials(true)
 *         .maxAge(3600);
 * </pre>
 * <p>
 * <b>Multiple Origins Support:</b>
 * <p>
 * For supporting multiple frontend deployments:
 * <pre>
 * registry.addMapping("/**")
 *         .allowedOriginPatterns("https://.*\\.example\\.com")  // Regex pattern
 *         .allowedMethods("GET", "POST", "PUT", "DELETE")
 *         .allowedHeaders("Content-Type", "Authorization")
 *         .allowCredentials(true);
 * </pre>
 * <p>
 * <b>Related Classes:</b>
 * <ul>
 *     <li>{@link com.trendythread.app.config.security.SecurityConfig} - Spring Security configuration</li>
 *     <li>{@link com.trendythread.app.filter.JwtSecurityFilter} - JWT token validation filter</li>
 * </ul>
 * <p>
 * <b>Testing CORS:</b>
 * <p>
 * Test if CORS is working from your frontend:
 * <pre>
 * // JavaScript/fetch
 * fetch('http://localhost:8079/api/v1/bloggers', {
 *   method: 'GET',
 *   headers: {
 *     'Authorization': 'Bearer ' + token,
 *     'Content-Type': 'application/json'
 *   }
 * })
 * .then(response => response.json())
 * .then(data => console.log('Success:', data))
 * .catch(error => console.error('CORS Error:', error));
 * </pre>
 * <p>
 * If CORS fails, browser console shows error like:
 * <pre>
 * Access to fetch at 'http://localhost:8079/api/v1/bloggers' from origin
 * 'http://localhost:3000' has been blocked by CORS policy
 * </pre>
 *
 * @author TrendyThread Development Team
 * @version 1.0
 * @since Spring Boot 3.x
 *
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer
 * @see org.springframework.web.servlet.config.annotation.CorsRegistry
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS">MDN - CORS Documentation</a>
 * @see <a href="https://spring.io/guides/gs/rest-service-cors/">Spring CORS Guide</a>
 */
@Configuration
public class CorsConfig {

    /**
     * Configure CORS mappings for all REST API endpoints.
     * <p>
     * This bean creates a Spring WebMvcConfigurer that registers CORS mappings
     * with the Spring MVC configuration. The configuration is applied globally
     * to all endpoints matching the path pattern (/**).
     * <p>
     * <b>Bean Name:</b> CORSConfiguration (can be used for reference/lookup)
     * <p>
     * <b>Initialization:</b> This bean is initialized during Spring application startup
     * and registered with the servlet context.
     * <p>
     * <b>Scope:</b> Singleton (one instance per application)
     * <p>
     * <b>Implementation Details:</b>
     * <ul>
     *     <li>Extends WebMvcConfigurer to customize Spring MVC configuration</li>
     *     <li>Overrides addCorsMappings() method to register CORS settings</li>
     *     <li>Calls parent's addCorsMappings() to preserve any other CORS configurations</li>
     * </ul>
     *
     * @return WebMvcConfigurer instance configured with CORS settings
     *
     * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer
     * @see #addCorsMappings(CorsRegistry) - The method that defines actual CORS rules
     */
    @Bean(name = "CORSConfiguration")
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            /**
             * Add CORS mappings to enable cross-origin requests.
             * <p>
             * This method is called by Spring during initialization to register CORS rules
             * for the application's REST endpoints. It configures which origins can make
             * requests, which HTTP methods are allowed, and which headers are permitted.
             * <p>
             * <b>Mapping Configuration Details:</b>
             * <p>
             * <b>1. Path Pattern: "/**"</b>
             * <pre>
             * registry.addMapping("/**")
             * </pre>
             * Applies CORS configuration to ALL endpoints in the application.
             * The "/**" pattern means: match any path of any length.
             * <p>
             * Alternative patterns:
             * <ul>
             *     <li>"/api/**" - Only API endpoints</li>
             *     <li>"/api/v1/**" - Only v1 API endpoints</li>
             *     <li>"/posts/**" - Only posts-related endpoints</li>
             * </ul>
             * <p>
             * <b>2. Allowed Origins: "http://localhost:3000"</b>
             * <pre>
             * .allowedOrigins("http://localhost:3000")
             * </pre>
             * Restricts CORS requests to only come from http://localhost:3000.
             * This is the typical port for frontend development (React, Vue, Angular).
             * <p>
             * Scheme, host, and port must all match exactly:
             * <ul>
             *     <li>✅ http://localhost:3000 - ALLOWED</li>
             *     <li>❌ http://localhost:3001 - BLOCKED (different port)</li>
             *     <li>❌ http://127.0.0.1:3000 - BLOCKED (different host)</li>
             *     <li>❌ https://localhost:3000 - BLOCKED (different scheme)</li>
             * </ul>
             * <p>
             * <b>For Production:</b> Change to your actual frontend domain:
             * <pre>
             * .allowedOrigins("https://example.com")
             * </pre>
             * <p>
             * <b>3. Allowed Methods: GET, POST, PUT, DELETE, OPTIONS</b>
             * <pre>
             * .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
             * </pre>
             * Specifies which HTTP methods the frontend is allowed to use.
             * <ul>
             *     <li><b>GET:</b> Retrieve data (fetch posts, bloggers, comments)</li>
             *     <li><b>POST:</b> Create new resources (create posts, comments)</li>
             *     <li><b>PUT:</b> Update existing resources (update user profile, posts)</li>
             *     <li><b>DELETE:</b> Remove resources (delete posts, comments)</li>
             *     <li><b>OPTIONS:</b> Preflight requests (sent automatically by browser)</li>
             * </ul>
             * <p>
             * Only include methods that your frontend actually needs. For example,
             * if frontend can't delete, don't allow DELETE method.
             * <p>
             * <b>4. Allowed Headers: "*"</b>
             * <pre>
             * .allowedHeaders("*")
             * </pre>
             * Allows ANY header in the request. The "*" wildcard means all headers are permitted.
             * <p>
             * Common headers used:
             * <ul>
             *     <li><b>Content-Type:</b> Specifies request body format (application/json)</li>
             *     <li><b>Authorization:</b> Contains JWT token (Bearer &lt;token&gt;)</li>
             *     <li><b>Accept:</b> Specifies expected response format (application/json)</li>
             *     <li><b>X-Requested-With:</b> Identifies AJAX requests</li>
             *     <li><b>Cache-Control:</b> Cache directives</li>
             * </ul>
             * <p>
             * <b>For Production (More Restrictive):</b>
             * <pre>
             * .allowedHeaders("Content-Type", "Authorization", "Accept")
             * </pre>
             * <p>
             * <b>5. Allow Credentials: true</b>
             * <pre>
             * .allowCredentials(true)
             * </pre>
             * Enables sending credentials (cookies, authorization headers) with cross-origin requests.
             * <p>
             * This is REQUIRED for JWT authentication because the Authorization header
             * must be included with each request:
             * <pre>
             * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
             * </pre>
             * <p>
             * <b>Important Note:</b> When allowCredentials is true, the allowed origins
             * CANNOT be "*" (wildcard). You must specify explicit origins for security.
             * <p>
             * <b>6. Call Parent Implementation:</b>
             * <pre>
             * WebMvcConfigurer.super.addCorsMappings(registry);
             * </pre>
             * Calls the parent class's addCorsMappings() method to preserve any other
             * CORS configurations that might have been registered elsewhere.
             * <p>
             * <b>Request/Response Example:</b>
             * <p>
             * <b>Frontend Request (from http://localhost:3000):</b>
             * <pre>
             * fetch('http://localhost:8079/api/v1/bloggers', {
             *   method: 'GET',
             *   headers: {
             *     'Authorization': 'Bearer eyJhbGc...',
             *     'Content-Type': 'application/json'
             *   }
             * })
             * </pre>
             * <p>
             * <b>Browser Sends Preflight (OPTIONS):</b>
             * <pre>
             * OPTIONS /api/v1/bloggers HTTP/1.1
             * Host: localhost:8079
             * Origin: http://localhost:3000
             * Access-Control-Request-Method: GET
             * Access-Control-Request-Headers: authorization, content-type
             * </pre>
             * <p>
             * <b>Server Response (from this configuration):</b>
             * <pre>
             * HTTP/1.1 200 OK
             * Access-Control-Allow-Origin: http://localhost:3000
             * Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
             * Access-Control-Allow-Headers: *
             * Access-Control-Allow-Credentials: true
             * Access-Control-Max-Age: 3600
             * </pre>
             * <p>
             * <b>Browser Then Sends Actual Request:</b>
             * <pre>
             * GET /api/v1/bloggers HTTP/1.1
             * Host: localhost:8079
             * Origin: http://localhost:3000
             * Authorization: Bearer eyJhbGc...
             * </pre>
             * <p>
             * <b>Server Response with Data:</b>
             * <pre>
             * HTTP/1.1 200 OK
             * Access-Control-Allow-Origin: http://localhost:3000
             * Access-Control-Allow-Credentials: true
             * Content-Type: application/json
             * [... blogger data ...]
             * </pre>
             * <p>
             * <b>Troubleshooting CORS Issues:</b>
             * <p>
             * <b>Issue 1: Browser blocks request with error:</b>
             * <pre>
             * Access to fetch at 'http://localhost:8079/...' from origin
             * 'http://localhost:3000' has been blocked by CORS policy
             * </pre>
             * <b>Solution:</b> Check if origin matches (scheme://host:port).
             * Use browser dev tools Network tab to see preflight response.
             * <p>
             * <b>Issue 2: Authorization header not sent:</b>
             * <pre>
             * Request header field Authorization is not allowed by
             * Access-Control-Allow-Headers
             * </pre>
             * <b>Solution:</b> Authorization header is already in allowed headers ("*").
             * Check that allowCredentials is true.
             * <p>
             * <b>Issue 3: Credentials not being sent:</b>
             * <pre>
             * // Frontend code
             * fetch(url, {
             *   credentials: 'include'  // Must include this for cookies
             * })
             * </pre>
             * <b>Solution:</b> Frontend must set credentials: 'include' in fetch options.
             *
             * @param registry The CorsRegistry provided by Spring for configuring CORS
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
                WebMvcConfigurer.super.addCorsMappings(registry);
            }
        };
    }
}
