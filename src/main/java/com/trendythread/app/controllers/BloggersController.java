package com.trendythread.app.controllers;

import com.trendythread.app.dto.BloggerDto;
import com.trendythread.app.entities.Blogger;
import com.trendythread.app.payloads.ApiResponse;
import com.trendythread.app.services.BloggersService;
import com.trendythread.app.services.impl.security.UserDetailsServiceImpl;
import com.trendythread.app.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "CRUD REST APIs for USER in TrendyThread",
        description = "CRUD REST APIs in TrendyThread to CREATE, UPDATE, FETCH and DELETE blogger details"
)
@RestController
@RequestMapping("/api/v1/bloggers")
@Slf4j
public class BloggersController {

    @Autowired
    private BloggersService bloggersService;

    @Operation(
            summary = "FETCH Current Authenticated User Profile REST API",
            description = "REST API to fetch the currently authenticated user's profile details",
            security = @SecurityRequirement(name = "Bearer")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping("/me")
    public ResponseEntity<BloggerDto> fetchCurrentUserProfile() {
        log.info("GET /api/v1/bloggers/me - fetchCurrentUserProfile request received");
        
        // Get the authenticated user's email from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // This returns the email (username)
        
        log.debug("Fetching profile for authenticated user: {}", email);
        
        // Fetch the blogger by email
        BloggerDto blogger = bloggersService.findByEmail(email);
        if (blogger == null) {
            log.error("Authenticated user not found in database: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        log.debug("GET /api/v1/bloggers/me - fetched blogger: {}", blogger);
        return ResponseEntity.ok(blogger);
    }

    @Operation(
            summary = "FETCH Blogger REST API",
            description = "REST API to fetch a Blogger details based on blogger-id"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping("/{bloggerId}")
    public ResponseEntity<BloggerDto> fetchByBloggerId(@PathVariable Integer bloggerId) {
        log.info("GET /api/v1/bloggers/{} - fetchByBloggerId request received", bloggerId);
        BloggerDto bloggerDto = bloggersService.findByBloggerId(bloggerId);
        log.debug("GET /api/v1/bloggers/{} - fetched blogger: {}", bloggerId, bloggerDto);
        return ResponseEntity.ok(bloggerDto);
    }

    @Operation(
            summary = "FETCH Blogger REST API",
            description = "REST API to fetch all Bloggers in TrendyThread"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping
    public ResponseEntity<List<BloggerDto>> fetchAllBloggers() {
        log.info("GET /api/v1/bloggers - fetchAllBloggers request received");
        List<BloggerDto> bloggerDtoList = bloggersService.fetchAllBloggers();
        log.debug("GET /api/v1/bloggers - fetched {} bloggers", bloggerDtoList == null ? 0 : bloggerDtoList.size());
        return ResponseEntity.ok(bloggerDtoList);
    }

    @Operation(
            summary = "UPDATE Blogger Details REST API",
            description = "REST API to update a Blogger based on blogger-id"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @PutMapping("/{bloggerId}")
    public ResponseEntity<BloggerDto> updateByBloggerId(@PathVariable Integer bloggerId, @Valid @RequestBody BloggerDto bloggerDto) {
        log.info("PUT /api/v1/bloggers/{} - updateByBloggerId request received: bloggerDto={}", bloggerId, bloggerDto);
        BloggerDto updatedBloggerDto = bloggersService.updateByBloggerId(bloggerDto, bloggerId);
        log.info("PUT /api/v1/bloggers/{} - update successful: {}", bloggerId, updatedBloggerDto);
        return ResponseEntity.ok(updatedBloggerDto);
    }

    @Operation(
            summary = "DELETE Blogger REST API",
            description = "REST API to delete a Blogger based on blogger-id"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @DeleteMapping("/{bloggerId}")
    public ResponseEntity<ApiResponse> deleteByBloggerId(@PathVariable Integer bloggerId) {
        log.info("DELETE /api/v1/bloggers/{} - deleteByBloggerId request received", bloggerId);
        this.bloggersService.deleteByBloggerId(bloggerId);
        log.info("DELETE /api/v1/bloggers/{} - deletion completed", bloggerId);
//        return new ResponseEntity<>(Map.of("message", "Blogger Deleted Successfully with Blogger-Id " + bloggerId), HttpStatus.OK);
        return new ResponseEntity<ApiResponse>(new ApiResponse("Blogger Deleted Successfully with Blogger-Id " + bloggerId, true), HttpStatus.OK);
    }
}
