package com.trendythread.app.controllers;

import com.trendythread.app.dto.BloggerDto;
import com.trendythread.app.payloads.ApiResponse;
import com.trendythread.app.services.BloggersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
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
            summary = "CREATE Blogger REST API",
            description = "REST API to create new Blogger in TrendyThread"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "HTTP Status CREATED"
    )
    @PostMapping
    public ResponseEntity<BloggerDto> createBlogger(@Valid @RequestBody BloggerDto bloggerDto) {
        log.info("POST /api/v1/bloggers - createBlogger request received: bloggerDto={}", bloggerDto);
        BloggerDto savedBloggerDto = bloggersService.createBlogger(bloggerDto);
        log.info("POST /api/v1/bloggers - blogger created: {}", savedBloggerDto);
        return new ResponseEntity<>(savedBloggerDto, HttpStatus.CREATED);
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
