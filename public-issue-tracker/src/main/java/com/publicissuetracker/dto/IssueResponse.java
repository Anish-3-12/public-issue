package com.publicissuetracker.dto;

import java.time.Instant;

public class IssueResponse {
    public String id;
    public String title;
    public String description;
    public String category;
    public String status;

    public Double latitude;
    public Double longitude;
    public String address;

    public String createdById;
    public String createdByName;

    public String assignedToId;
    public String assignedToName;

    public Instant createdAt;
    public Instant updatedAt;
    public Instant resolvedAt;
    public Instant verifiedAt;

    public Integer upvoteCount;
}

