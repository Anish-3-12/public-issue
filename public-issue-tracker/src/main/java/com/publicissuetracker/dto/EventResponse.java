package com.publicissuetracker.dto;

import java.time.Instant;

public class EventResponse {
    public String id;
    public String issueId;
    public String type;
    public String actorId;
    public String fromStatus;
    public String toStatus;
    public String note;
    public Instant createdAt;
}

