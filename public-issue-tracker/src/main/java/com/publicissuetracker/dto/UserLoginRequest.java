package com.publicissuetracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserLoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    public String email;

    @NotBlank(message = "Password is required")
    public String password;
}
