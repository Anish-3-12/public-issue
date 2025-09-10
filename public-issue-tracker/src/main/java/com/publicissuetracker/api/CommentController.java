package com.publicissuetracker.api;

import com.publicissuetracker.dto.CommentCreateRequest;
import com.publicissuetracker.dto.CommentResponse;
import com.publicissuetracker.model.User;
import com.publicissuetracker.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/issues/{issueId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Add a new comment to an issue.
     */
    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable("issueId") String issueId,
            @Valid @RequestBody CommentCreateRequest req
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User author = (User) principal;

        CommentResponse created = commentService.createComment(issueId, req, author);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * List all comments for an issue.
     */
    @GetMapping
    public ResponseEntity<List<CommentResponse>> listComments(@PathVariable("issueId") String issueId) {
        return ResponseEntity.ok(commentService.listComments(issueId));
    }
}


