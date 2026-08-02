package com.trendythread.app.controllers;

import com.trendythread.app.services.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(
        name = "Tags",
        description = "Public listing of tags in use, for tag clouds/autocomplete"
)
@RestController
@RequestMapping("/api/v1/tags")
@Slf4j
public class TagController {

    @Autowired
    private TagService tagService;

    @Operation(
            summary = "List All Tags",
            description = "Returns every tag name currently in use, sorted alphabetically."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping
    public ResponseEntity<List<String>> getAllTags() {
        log.info("GET /api/v1/tags - getAllTags request received");
        return ResponseEntity.ok(tagService.findAllTagNames());
    }
}
