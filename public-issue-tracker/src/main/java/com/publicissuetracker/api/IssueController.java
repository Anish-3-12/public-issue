package com.publicissuetracker.api;

import com.publicissuetracker.dto.CommentCreateRequest;
import com.publicissuetracker.dto.CommentResponse;
import com.publicissuetracker.dto.EventResponse;
import com.publicissuetracker.dto.IssueCreateRequest;
import com.publicissuetracker.dto.IssueResponse;
import com.publicissuetracker.model.IssueEvent;
import com.publicissuetracker.model.User;
import com.publicissuetracker.repository.IssueEventRepository;
import com.publicissuetracker.repository.UserRepository;
import com.publicissuetracker.service.CommentService;
import com.publicissuetracker.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * IssueController - handles creating/listing/getting issues,
 * updating status, assigning, and listing timeline events.
 *
 * Note: comment listing/creation is handled in CommentController (to avoid duplicate mappings).
 */
@RestController
@RequestMapping("/api/v1/issues")
public class IssueController {

    private final IssueService issueService;
    private final UserRepository userRepository;
    private final CommentService commentService;
    private final IssueEventRepository issueEventRepository;

    public IssueController(IssueService issueService,
                           UserRepository userRepository,
                           CommentService commentService,
                           IssueEventRepository issueEventRepository) {
        this.issueService = issueService;
        this.userRepository = userRepository;
        this.commentService = commentService;
        this.issueEventRepository = issueEventRepository;
    }

    /**
     * Create a new issue (citizen reports it).
     * The authenticated user is taken from the SecurityContext (set by JwtAuthenticationFilter).
     */
    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(@Valid @RequestBody IssueCreateRequest req) {
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
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<IssueResponse> updateStatus(
            @PathVariable String id,
            @RequestParam String status
    ) {
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
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User acting = (User) principal;

        if (req == null || req.getAssignedToId() == null || req.getAssignedToId().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        if (!userRepository.findById(req.getAssignedToId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        return issueService.assign(id, req.getAssignedToId(), acting)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * List events (timeline) for an issue.
     *
     * GET /api/v1/issues/{id}/events
     */
    @GetMapping("/{id}/events")
    public ResponseEntity<List<EventResponse>> listEvents(@PathVariable("id") String issueId) {
        List<IssueEvent> events = issueEventRepository.findByIssueIdOrderByCreatedAtAsc(issueId);
        List<EventResponse> resp = events.stream().map(e -> {
            EventResponse er = new EventResponse();
            er.id = e.getId();
            er.issueId = e.getIssueId();
            er.type = e.getType();
            er.actorId = e.getActorId();
            er.fromStatus = e.getFromStatus();
            er.toStatus = e.getToStatus();
            er.note = e.getNote();
            er.createdAt = e.getCreatedAt();
            return er;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
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
