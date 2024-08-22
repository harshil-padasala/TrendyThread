package com.trendythread.app.controllers;

import com.trendythread.app.payloads.ApiResponse;
import com.trendythread.app.dto.UserDto;
import com.trendythread.app.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
        UserDto savedUserDto = userService.createUser(userDto);
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
        UserDto userDto = userService.findByUserId(userId);
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
        List<UserDto> userDtoList = userService.fetchAllUsers();
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
        UserDto updatedUserDto = userService.updateByUserId(userDto, userId);
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
        this.userService.fetchByUserId(userId);
//        return new ResponseEntity<>(Map.of("message", "User Deleted Successfully with User-Id " + userId), HttpStatus.OK);
        return new ResponseEntity<ApiResponse>(new ApiResponse("User Deleted Successfully with User-Id " + userId, true), HttpStatus.OK);
    }
}
