package com.publicissuetracker.api;

import com.publicissuetracker.dto.IssueResponse;
import com.publicissuetracker.model.User;
import com.publicissuetracker.service.IssueService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final IssueService issueService;

    public MeController(IssueService issueService) {
        this.issueService = issueService;
    }

    /**
     * Returns issues reported by the currently authenticated user.
     * GET /api/v1/me/issues
     */
    @GetMapping("/issues")
    public ResponseEntity<List<IssueResponse>> myIssues() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            return ResponseEntity.status(401).build();
        }
        User me = (User) principal;
        List<IssueResponse> issues = issueService.listIssuesByReporterId(me.getId());
        return ResponseEntity.ok(issues);
    }
}
