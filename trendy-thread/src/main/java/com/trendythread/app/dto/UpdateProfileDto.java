package com.trendythread.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;


@Schema(
        name = "UpdateProfile",
        description = "Schema to hold Profile Update information (no password required)"
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileDto {

    @Schema(
            description = "The Blogger's first name, which must be at least 3 characters long.",
            example = "John"
    )
    @Valid
    @Size(min = 3, max = 20, message = "First name cannot be empty and must be min of 3 characters!!!")
    @NotEmpty(message = "First Name cannot be empty")
    private String firstName;

    @Schema(
            description = "The Blogger's last name, which must be at least 3 characters long.",
            example = "Doe"
    )
    @Valid
    @Size(min = 3, max = 20, message = "LastName cannot be empty and must be min of 3 characters!!!")
    @NotEmpty(message = "Last Name cannot be empty")
    private String lastName;

    @Schema(
            description = "Additional information or bio about the Blogger.",
            example = "A software developer specializing in backend systems."
    )
    @Size(max = 500, message = "About section cannot exceed 500 characters")
    private String about;
}
