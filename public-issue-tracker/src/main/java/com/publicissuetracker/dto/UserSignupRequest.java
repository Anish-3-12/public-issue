package com.publicissuetracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserSignupRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 200)
    public String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    @Size(max = 255)
    public String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    public String password;
}

