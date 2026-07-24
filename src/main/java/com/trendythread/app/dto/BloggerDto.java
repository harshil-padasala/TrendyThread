package com.trendythread.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;


@Schema(
        name = "Blogger",
        description = "Schema to hold Blogger information"
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BloggerDto {

    @Schema(
            description = "Unique identifier for the Blogger.",
            example = "123"
    )
    private int id;

    @Schema(
            description = "The Blogger's username, which must be at least 3 characters long.",
            example = "John Doe"
    )
    @Valid
    @Size(min = 3, max = 20, message = "UserName cannot be empty and must be min of 3 characters!!!")
    @NotEmpty(message = "UserName cannot be empty")
    private String userName;

    @Schema(
            description = "The Blogger's first name, which must be at least 3 characters long.",
            example = "John Doe"
    )
    @Valid
    @Size(min = 3, max = 20, message = "First name cannot be empty and must be min of 3 characters!!!")
    @NotEmpty(message = "First Name cannot be empty")
    private String firstName;

    @Schema(
            description = "The Blogger's last name, which must be at least 3 characters long.",
            example = "John Doe"
    )
    @Valid
    @Size(min = 3, max = 20, message = "LastName cannot be empty and must be min of 3 characters!!!")
    @NotEmpty(message = "Last Name cannot be empty")
    private String lastName;

    @Schema(
            description = "The Blogger's email address, which must be a valid email format.",
            example = "john.doe@example.com"
    )
    @Email(message = "please enter valid email")
    @NotEmpty(message = "email cannot be empty")
    private String email;

    @Schema(
            description = "The Blogger's password, which must be at least 3 characters long. Ensure it is securely handled.",
            example = "P@ssword#123"
    )
    @Size(min = 3, max = 20, message = "password cannot be empty and must be min of 3 characters!!!")
    @NotEmpty(message = "password cannot be empty")
    private String password;

    @Schema(
            description = "Additional information or bio about the Blogger.",
            example = "A software developer specializing in backend systems."
    )
    private String about;

    @Schema(
            description = "The role of the Blogger in the system. Determines access permissions.",
            example = "ROLE_USER",
            allowableValues = {"ROLE_USER", "ROLE_ADMIN"}
    )
    private String role;
}
