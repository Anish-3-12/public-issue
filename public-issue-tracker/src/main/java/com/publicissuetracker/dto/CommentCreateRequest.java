package com.publicissuetracker.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

/**
 * Accepts either JSON { "text": "..." } (preferred) or { "message": "..." } (legacy).
 */
public class CommentCreateRequest {

    @NotBlank(message = "text must not be blank")
    @JsonAlias({ "message", "text" })
    public String text;

    public CommentCreateRequest() {}

    public CommentCreateRequest(String text) {
        this.text = text;
    }

    // Jackson-friendly getters/setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

