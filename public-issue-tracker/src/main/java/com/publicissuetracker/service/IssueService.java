package com.publicissuetracker.service;

import com.publicissuetracker.dto.IssueCreateRequest;
import com.publicissuetracker.dto.IssueResponse;
import com.publicissuetracker.model.User;

import java.util.List;
import java.util.Optional;

public interface IssueService {

    /**
     * Report a new issue by a citizen.
     * @param req the issue creation request
     * @param createdBy the user who is reporting
     * @return the created issue
     */
    IssueResponse createIssue(IssueCreateRequest req, User createdBy);

    /**
     * Find an issue by ID.
     */
    Optional<IssueResponse> findById(String issueId);

    /**
     * List all issues.
     * Later we will add filters: status, category, location, etc.
     */
    List<IssueResponse> listIssues();

    /**
     * Update status of an issue (e.g., OPEN → RESOLVED).
     */
    Optional<IssueResponse> updateStatus(String issueId, String newStatus, User actingUser);

    /**
     * Assign an issue to another user (usually by an admin).
     *
     * @param issueId ID of the issue to assign
     * @param assignedToUserId ID of the user the issue will be assigned to
     * @param actingUser the admin performing the assignment
     * @return updated IssueResponse, if found
     */
    Optional<IssueResponse> assign(String issueId, String assignedToUserId, User actingUser);
}

