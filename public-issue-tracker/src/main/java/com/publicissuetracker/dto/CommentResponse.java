package com.publicissuetracker.dto;

import java.time.Instant;

public class CommentResponse {
    public String id;
    public String issueId;
    public String authorId;
    public String authorName; // controller will fill author name
    public String message;
    public Instant createdAt;
}

