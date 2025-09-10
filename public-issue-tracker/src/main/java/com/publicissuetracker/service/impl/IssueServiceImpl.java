package com.publicissuetracker.service.impl;

import com.publicissuetracker.dto.IssueCreateRequest;
import com.publicissuetracker.dto.IssueResponse;
import com.publicissuetracker.model.Issue;
import com.publicissuetracker.model.User;
import com.publicissuetracker.repository.IssueRepository;
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

    public IssueServiceImpl(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
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
            issue.setStatus(newStatus);

            if ("RESOLVED".equalsIgnoreCase(newStatus)) {
                issue.setResolvedAt(Instant.now());
            }
            if ("VERIFIED".equalsIgnoreCase(newStatus)) {
                issue.setVerifiedAt(Instant.now());
            }

            Issue updated = issueRepository.save(issue);
            return toResponse(updated);
        });
    }

    @Override
    public Optional<IssueResponse> assignIssue(String issueId, User adminUser) {
        return issueRepository.findById(issueId).map(issue -> {
            issue.setAssignedTo(adminUser);
            Issue updated = issueRepository.save(issue);
            return toResponse(updated);
        });
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
