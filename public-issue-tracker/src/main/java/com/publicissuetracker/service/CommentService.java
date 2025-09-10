package com.publicissuetracker.service;

import com.publicissuetracker.dto.CommentCreateRequest;
import com.publicissuetracker.dto.CommentResponse;
import com.publicissuetracker.model.User;

import java.util.List;

public interface CommentService {

    /**
     * Create a comment for an issue and return a response DTO.
     */
    CommentResponse createComment(String issueId, CommentCreateRequest req, User author);

    /**
     * List comments (as DTOs) for an issue.
     */
    List<CommentResponse> listComments(String issueId);
}


