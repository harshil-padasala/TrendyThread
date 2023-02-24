package com.blog.app.payloads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private int id;

    @Valid
    @Size(min = 3, message = "name cannot be empty and must be min of 3 characters!!!")
    private String name;

    @Email(message = "please enter valid email")
    @NotEmpty(message = "email cannot be empty")
    private String email;

    @Size(min = 3, message = "password cannot be empty and must be min of 3 characters!!!")
    private String password;

    private String about;
}
