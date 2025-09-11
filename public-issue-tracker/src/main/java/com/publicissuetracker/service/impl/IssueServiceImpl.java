package com.publicissuetracker.service.impl;

import com.publicissuetracker.dto.IssueCreateRequest;
import com.publicissuetracker.dto.IssueResponse;
import com.publicissuetracker.model.Issue;
import com.publicissuetracker.model.IssueEvent;
import com.publicissuetracker.model.User;
import com.publicissuetracker.repository.IssueEventRepository;
import com.publicissuetracker.repository.IssueRepository;
import com.publicissuetracker.repository.UserRepository;
import com.publicissuetracker.service.IssueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final IssueEventRepository issueEventRepository;

    // NOTE: add IssueEventRepository to constructor so Spring can autowire it
    public IssueServiceImpl(IssueRepository issueRepository,
                            UserRepository userRepository,
                            IssueEventRepository issueEventRepository) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.issueEventRepository = issueEventRepository;
    }

    @Override
    public IssueResponse createIssue(IssueCreateRequest req, User createdBy) {
        Issue issue = new Issue(
                req.title,
                req.description,
                req.category,
                req.latitude,
                req.longitude,
                req.address,
                createdBy
        );
        Issue saved = issueRepository.save(issue);

        // create an initial event for creation (optional)
        IssueEvent ev = new IssueEvent();
        ev.setIssueId(saved.getId());
        ev.setType("CREATED");
        ev.setActorId(createdBy != null ? createdBy.getId() : null);
        ev.setNote("Issue created");
        issueEventRepository.save(ev);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IssueResponse> findById(String issueId) {
        return issueRepository.findById(issueId).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueResponse> listIssues() {
        return issueRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<IssueResponse> updateStatus(String issueId, String newStatus, User actingUser) {
        return issueRepository.findById(issueId).map(issue -> {
            // capture previous status before changing
            String previousStatus = issue.getStatus();

            issue.setStatus(newStatus);

            if ("RESOLVED".equalsIgnoreCase(newStatus)) {
                issue.setResolvedAt(Instant.now());
            }
            if ("VERIFIED".equalsIgnoreCase(newStatus)) {
                issue.setVerifiedAt(Instant.now());
            }
            issue.setUpdatedAt(Instant.now());

            Issue updated = issueRepository.save(issue);

            // persist an IssueEvent for this status change
            IssueEvent ev = new IssueEvent();
            ev.setIssueId(issue.getId());
            ev.setType("STATUS_CHANGE");
            ev.setActorId(actingUser != null ? actingUser.getId() : null);
            ev.setFromStatus(previousStatus);
            ev.setToStatus(newStatus);
            ev.setNote(null);
            issueEventRepository.save(ev);

            return toResponse(updated);
        });
    }

    /**
     * Assign an issue to another user (admin performing the assignment).
     *
     * @param issueId ID of the issue to assign
     * @param assignedToUserId ID of the user that will be assigned
     * @param actingUser the admin performing the assignment
     * @return Optional containing updated IssueResponse if assignment succeeded
     */
    @Override
    public Optional<IssueResponse> assign(String issueId, String assignedToUserId, User actingUser) {
        // find the assignee user
        Optional<User> assigneeOpt = userRepository.findById(assignedToUserId);
        if (!assigneeOpt.isPresent()) {
            // assignee not found -> cannot assign
            return Optional.empty();
        }
        User assignee = assigneeOpt.get();

        // find issue and set assignee
        return issueRepository.findById(issueId).map(issue -> {
            issue.setAssignedTo(assignee);
            issue.setUpdatedAt(Instant.now());
            Issue updated = issueRepository.save(issue);

            // create IssueEvent of type ASSIGNMENT
            IssueEvent ev = new IssueEvent();
            ev.setIssueId(issue.getId());
            ev.setType("ASSIGNMENT");
            ev.setActorId(actingUser != null ? actingUser.getId() : null);
            ev.setFromStatus(issue.getStatus()); // note: this is the current status
            ev.setToStatus(issue.getStatus());
            ev.setNote("Assigned to user: " + assignee.getId());
            issueEventRepository.save(ev);

            return toResponse(updated);
        });
    }

    /**
     * Return issues reported by a specific reporter (reporterId).
     * Uses IssueRepository.findByReporterIdOrderByCreatedAtDesc(...)
     */
    @Override
    @Transactional(readOnly = true)
    public List<IssueResponse> listIssuesByReporterId(String reporterId) {
        List<Issue> issues = issueRepository.findByReporterIdOrderByCreatedAtDesc(reporterId);
        return issues.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // --- helper method ---
    private IssueResponse toResponse(Issue i) {
        IssueResponse r = new IssueResponse();
        r.id = i.getId();
        r.title = i.getTitle();
        r.description = i.getDescription();
        r.category = i.getCategory();
        r.status = i.getStatus();
        r.latitude = i.getLatitude();
        r.longitude = i.getLongitude();
        r.address = i.getAddress();

        if (i.getCreatedBy() != null) {
            r.createdById = i.getCreatedBy().getId();
            r.createdByName = i.getCreatedBy().getName();
        }
        if (i.getAssignedTo() != null) {
            r.assignedToId = i.getAssignedTo().getId();
            r.assignedToName = i.getAssignedTo().getName();
        }

        r.createdAt = i.getCreatedAt();
        r.updatedAt = i.getUpdatedAt();
        r.resolvedAt = i.getResolvedAt();
        r.verifiedAt = i.getVerifiedAt();
        r.upvoteCount = i.getUpvoteCount();

        return r;
    }
}
