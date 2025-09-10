package com.publicissuetracker.api;

import com.publicissuetracker.repository.IssueRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Small admin controller exposing a few metrics for admins.
 * Only users with ROLE_ADMIN can call these endpoints (JwtAuthenticationFilter already sets ROLE_ prefix).
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final IssueRepository issueRepository;

    public AdminController(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    /**
     * GET /api/v1/admin/issues/metrics
     * Response:
     * {
     *   "total": 123,
     *   "open": 45,
     *   "inProgress": 10,
     *   "resolved": 60
     * }
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/issues/metrics")
    public ResponseEntity<Map<String, Object>> issueMetrics() {
        long total = issueRepository.count();
        long open = issueRepository.countByStatus("OPEN");
        long inProgress = issueRepository.countByStatus("IN_PROGRESS");
        long resolved = issueRepository.countByStatus("RESOLVED");

        Map<String, Object> body = Map.of(
                "total", total,
                "open", open,
                "inProgress", inProgress,
                "resolved", resolved
        );
        return ResponseEntity.ok(body);
    }
}

