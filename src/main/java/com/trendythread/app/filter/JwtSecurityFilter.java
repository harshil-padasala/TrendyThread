package com.trendythread.app.filter;

import com.trendythread.app.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
@Slf4j
public class JwtSecurityFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.info("JwtSecurityFilter - doFilterInternal - request received: method={}, uri={}", request.getMethod(), request.getRequestURI());
        String authHeader = request.getHeader("Authorization");
        String userName = null;
        String jwtToken = null;

        try {
            if (Objects.nonNull(authHeader) && authHeader.startsWith("Bearer ")) {
                jwtToken = authHeader.substring(7);
                log.debug("JwtSecurityFilter - extracted JWT token from Authorization header");

                try {
                    userName = jwtUtil.extractUsername(jwtToken);
                    log.debug("JwtSecurityFilter - extracted username from JWT token: {}", userName);
                } catch (Exception e) {
                    log.warn("JwtSecurityFilter - failed to extract username from JWT token: {}", e.getMessage(), e);
                    userName = null;
                }
            } else if (Objects.nonNull(authHeader)) {
                log.warn("JwtSecurityFilter - Authorization header does not start with 'Bearer ': {}", authHeader);
                filterChain.doFilter(request, response);
                return;
            } else {
                log.debug("JwtSecurityFilter - no Authorization header found in request");
                filterChain.doFilter(request, response);
                return;
            }

            if (Objects.nonNull(userName)) {
                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
                    log.debug("JwtSecurityFilter - loaded UserDetails for username: {}", userName);

                    if (jwtUtil.isTokenValid(jwtToken, userDetails.getUsername())) {
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.info("JwtSecurityFilter - JWT token is valid. Authentication set in SecurityContext for user: {}", userName);
                    } else {
                        log.warn("JwtSecurityFilter - JWT token validation failed for user: {}", userName);
                    }
                } catch (Exception e) {
                    log.warn("JwtSecurityFilter - error validating token or loading user: {}", e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("JwtSecurityFilter - unexpected error in filter: {}", e.getMessage(), e);
        }

        log.debug("JwtSecurityFilter - proceeding with filter chain for request: method={}, uri={}", request.getMethod(), request.getRequestURI());
        filterChain.doFilter(request, response);
    }
}
