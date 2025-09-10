package com.publicissuetracker.api;

import com.publicissuetracker.model.IssueComment;
import com.publicissuetracker.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/issues/{id}/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) { this.commentService = commentService; }

    @PostMapping
    public ResponseEntity<IssueComment> add(@PathVariable("id") String id,
                                            @RequestBody AddCommentRequest req,
                                            Principal principal) {
        String userId = principal.getName(); // or from JWT
        IssueComment created = commentService.addComment(id, userId, req.getMessage());
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public List<IssueComment> list(@PathVariable("id") String id) {
        return commentService.listComments(id);
    }

    static class AddCommentRequest { private String message; public String getMessage(){return message;} public void setMessage(String m){this.message=m;} }
}

