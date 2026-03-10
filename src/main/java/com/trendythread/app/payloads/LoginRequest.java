package com.trendythread.app.payloads;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login request payload")
public class LoginRequest {

    @Schema(description = "User email address", example = "user@example.com")
    private String email;

    @Schema(description = "User password", example = "password123")
    private String password;
}
