package com.publicissuetracker.api;

import com.publicissuetracker.model.Issue;
import com.publicissuetracker.repository.IssueRepository;
import com.publicissuetracker.service.AttachmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/issues")
public class AttachmentController {

    private final IssueRepository issueRepository;
    private final AttachmentService attachmentService;

    public AttachmentController(IssueRepository issueRepository, AttachmentService attachmentService) {
        this.issueRepository = issueRepository;
        this.attachmentService = attachmentService;
    }

    // Upload one or more files
    @PostMapping("/{id}/attachments")
    public ResponseEntity<?> upload(
            @PathVariable("id") String id,                 // keep String to match repository
            @RequestParam("files") List<MultipartFile> files
    ) {
        // directly use String id with repository
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found: " + id));

        attachmentService.uploadAttachments(issue, files);
        return ResponseEntity.ok().body("Uploaded " + files.size() + " file(s)");
    }

    // List all attachments for an issue
    @GetMapping("/{id}/attachments")
    public ResponseEntity<List<String>> list(@PathVariable("id") String id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found: " + id));

        List<String> urls = attachmentService.listAttachments(issue);
        return ResponseEntity.ok(urls);
    }
}

