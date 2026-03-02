package com.trendythread.app.controllers;

import com.trendythread.app.payloads.ApiResponse;
import com.trendythread.app.dto.UserDto;
import com.trendythread.app.services.UserService;
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

import javax.validation.Valid;
import java.util.List;

@Tag(
        name = "CRUD REST APIs for USER in TrendyThread",
        description = "CRUD REST APIs in TrendyThread to CREATE, UPDATE, FETCH and DELETE user details"
)
@RestController
@RequestMapping("/api/v1/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(
            summary = "CREATE User REST API",
            description = "REST API to create new User in TrendyThread"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "HTTP Status CREATED"
    )
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        log.info("POST /api/v1/users - createUser request received: userDto={}", userDto);
        UserDto savedUserDto = userService.createUser(userDto);
        log.info("POST /api/v1/users - user created: {}", savedUserDto);
        return new ResponseEntity<>(savedUserDto, HttpStatus.CREATED);
    }

    @Operation(
            summary = "FETCH User REST API",
            description = "REST API to fetch a User details based on user-id"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> fetchByUserId(@PathVariable Integer userId) {
        log.info("GET /api/v1/users/{} - fetchByUserId request received", userId);
        UserDto userDto = userService.findByUserId(userId);
        log.debug("GET /api/v1/users/{} - fetched user: {}", userId, userDto);
        return ResponseEntity.ok(userDto);
    }

    @Operation(
            summary = "FETCH User REST API",
            description = "REST API to fetch all Users in TrendyThread"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping
    public ResponseEntity<List<UserDto>> fetchAllUsers() {
        log.info("GET /api/v1/users - fetchAllUsers request received");
        List<UserDto> userDtoList = userService.fetchAllUsers();
        log.debug("GET /api/v1/users - fetched {} users", userDtoList == null ? 0 : userDtoList.size());
        return ResponseEntity.ok(userDtoList);
    }

    @Operation(
            summary = "UPDATE User Details REST API",
            description = "REST API to update a User based on user-id"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateByUserId(@PathVariable Integer userId, @Valid @RequestBody UserDto userDto) {
        log.info("PUT /api/v1/users/{} - updateByUserId request received: userDto={}", userId, userDto);
        UserDto updatedUserDto = userService.updateByUserId(userDto, userId);
        log.info("PUT /api/v1/users/{} - update successful: {}", userId, updatedUserDto);
        return ResponseEntity.ok(updatedUserDto);
    }

    @Operation(
            summary = "DELETE User REST API",
            description = "REST API to delete a User based on user-id"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse> deleteByUserId(@PathVariable Integer userId) {
        log.info("DELETE /api/v1/users/{} - deleteByUserId request received", userId);
        this.userService.fetchByUserId(userId);
        log.info("DELETE /api/v1/users/{} - deletion completed", userId);
//        return new ResponseEntity<>(Map.of("message", "User Deleted Successfully with User-Id " + userId), HttpStatus.OK);
        return new ResponseEntity<ApiResponse>(new ApiResponse("User Deleted Successfully with User-Id " + userId, true), HttpStatus.OK);
    }
}
