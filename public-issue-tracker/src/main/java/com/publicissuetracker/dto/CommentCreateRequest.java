package com.publicissuetracker.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentCreateRequest {
    @NotBlank
    public String message;

    // optional: default ctor / getters/setters if you prefer
    public CommentCreateRequest() {}
    public CommentCreateRequest(String message) { this.message = message; }
}

