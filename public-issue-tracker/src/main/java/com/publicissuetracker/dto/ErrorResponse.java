package com.publicissuetracker.dto;

import java.time.Instant;

public class ErrorResponse {
    private String error;
    private int status;
    private Instant timestamp;

    public ErrorResponse(String error, int status) {
        this.error = error;
        this.status = status;
        this.timestamp = Instant.now();
    }

    // --- getters and setters ---

    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }

    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}

