package com.trendythread.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;


@Schema(
        name = "User",
        description = "Schema to hold User information"
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    @Schema(
            description = "Unique identifier for the user.",
            example = "123"
    )
    private int id;

    @Schema(
            description = "The user's name, which must be at least 3 characters long.",
            example = "John Doe"
    )
    @Valid
    @Size(min = 3, max = 20, message = "name cannot be empty and must be min of 3 characters!!!")
    @NotEmpty(message = "name cannot be empty")
    private String name;

    @Schema(
            description = "The user's email address, which must be a valid email format.",
            example = "john.doe@example.com"
    )
    @Email(message = "please enter valid email")
    @NotEmpty(message = "email cannot be empty")
    private String email;

    @Schema(
            description = "The user's password, which must be at least 3 characters long. Ensure it is securely handled.",
            example = "P@ssword#123"
    )
    @Size(min = 3, max = 20, message = "password cannot be empty and must be min of 3 characters!!!")
    @NotEmpty(message = "password cannot be empty")
    private String password;

    @Schema(
            description = "Additional information or bio about the user.",
            example = "A software developer specializing in backend systems."
    )
    private String about;
}
