package com.publicissuetracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class IssueCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 400)
    public String title;

    @NotBlank(message = "Description is required")
    public String description;

    @NotBlank(message = "Category is required")
    public String category;

    @NotNull(message = "Latitude is required")
    public Double latitude;

    @NotNull(message = "Longitude is required")
    public Double longitude;

    public String address; // optional
}

