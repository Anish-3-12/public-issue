package com.publicissuetracker.api;

import com.publicissuetracker.dto.CommentCreateRequest;
import com.publicissuetracker.dto.CommentResponse;
import com.publicissuetracker.model.User;
import com.publicissuetracker.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller responsible for comment-related operations for an issue.
 *
 * Base path: /api/v1/issues/{issueId}/comments
 */
@RestController
@RequestMapping("/api/v1/issues/{issueId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Add a new comment to an issue.
     *
     * POST /api/v1/issues/{issueId}/comments
     */
    @PostMapping
    public ResponseEntity<?> addComment(
            @PathVariable("issueId") String issueId,
            @Valid @RequestBody CommentCreateRequest req,
            BindingResult bindingResult
    ) {
        // validation errors -> return 400 with simple message
        if (bindingResult != null && bindingResult.hasErrors()) {
            String msg = bindingResult.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("; "));
            return ResponseEntity.badRequest().body(msg);
        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User author = (User) principal;

        try {
            CommentResponse created = commentService.createComment(issueId, req, author);
            // ensure author name present in response if the service didn't set it
            if (created != null) {
                if (created.authorName == null || created.authorName.isBlank()) {
                    created.authorName = author.getName();
                }
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException iae) {
            // service may throw for "issue not found" or invalid args
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(iae.getMessage());
        } catch (Exception ex) {
            // log server-side in real app; return 500 to client
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add comment");
        }
    }

    /**
     * List all comments for an issue.
     *
     * GET /api/v1/issues/{issueId}/comments
     */
    @GetMapping
    public ResponseEntity<List<CommentResponse>> listComments(@PathVariable("issueId") String issueId) {
        List<CommentResponse> list = commentService.listComments(issueId);
        return ResponseEntity.ok(list);
    }
}


