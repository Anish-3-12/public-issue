package com.publicissuetracker.api;

import com.publicissuetracker.dto.IssueCreateRequest;
import com.publicissuetracker.dto.IssueResponse;
import com.publicissuetracker.model.User;
import com.publicissuetracker.repository.UserRepository;
import com.publicissuetracker.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * IssueController - handles creating/listing/getting issues.
 *
 * Important:
 * - Admin-only operations (assigning and status updates) are protected with @PreAuthorize.
 * - We still fetch the authenticated principal from SecurityContextHolder to get the current User object.
 */
@RestController
@RequestMapping("/api/v1/issues")
public class IssueController {

    private final IssueService issueService;
    private final UserRepository userRepository;

    public IssueController(IssueService issueService, UserRepository userRepository) {
        this.issueService = issueService;
        this.userRepository = userRepository;
    }

    /**
     * Create a new issue (citizen reports it).
     * The authenticated user is taken from the SecurityContext (set by JwtAuthenticationFilter).
     */
    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(@Valid @RequestBody IssueCreateRequest req) {
        // Get authenticated principal
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User currentUser = (User) principal;
        IssueResponse created = issueService.createIssue(req, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * List issues (public / authenticated users).
     */
    @GetMapping
    public ResponseEntity<List<IssueResponse>> listIssues() {
        return ResponseEntity.ok(issueService.listIssues());
    }

    /**
     * Get issue by id (details).
     */
    @GetMapping("/{id}")
    public ResponseEntity<IssueResponse> getIssue(@PathVariable String id) {
        return issueService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Admin-only: update issue status (OPEN -> IN_PROGRESS -> RESOLVED -> VERIFIED etc).
     *
     * Example call:
     * PATCH /api/v1/issues/{id}/status?status=RESOLVED
     *
     * Only users with ADMIN role can call this (enforced by PreAuthorize).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<IssueResponse> updateStatus(
            @PathVariable String id,
            @RequestParam String status
    ) {
        // acting user from SecurityContext
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User acting = (User) principal;

        return issueService.updateStatus(id, status, acting)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Admin-only: assign an issue to a user.
     *
     * POST /api/v1/issues/{id}/assign
     * body: { "assignedToId": "user-uuid" }
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/assign")
    public ResponseEntity<IssueResponse> assignIssue(
            @PathVariable String id,
            @RequestBody AssignRequest req
    ) {
        // acting user from SecurityContext
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User acting = (User) principal;

        // Basic validation for request payload
        if (req == null || req.getAssignedToId() == null || req.getAssignedToId().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        // Optionally verify the assigned-to user exists (helps early error)
        if (!userRepository.findById(req.getAssignedToId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        return issueService.assign(id, req.getAssignedToId(), acting)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Small DTO for assignment request. If you prefer a top-level class put it under dto/ folder.
     */
    public static class AssignRequest {
        private String assignedToId;

        public String getAssignedToId() {
            return assignedToId;
        }

        public void setAssignedToId(String assignedToId) {
            this.assignedToId = assignedToId;
        }
    }
}




