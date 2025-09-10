package com.publicissuetracker.service.impl;

import com.publicissuetracker.dto.CommentCreateRequest;
import com.publicissuetracker.dto.CommentResponse;
import com.publicissuetracker.model.IssueComment;
import com.publicissuetracker.model.IssueEvent;
import com.publicissuetracker.model.User;
import com.publicissuetracker.repository.IssueCommentRepository;
import com.publicissuetracker.repository.IssueEventRepository;
import com.publicissuetracker.repository.UserRepository;
import com.publicissuetracker.service.CommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final IssueCommentRepository commentRepo;
    private final IssueEventRepository eventRepo;
    private final UserRepository userRepository; // optional: enrich author name if needed

    public CommentServiceImpl(IssueCommentRepository commentRepo,
                              IssueEventRepository eventRepo,
                              UserRepository userRepository) {
        this.commentRepo = commentRepo;
        this.eventRepo = eventRepo;
        this.userRepository = userRepository;
    }

    @Override
    public CommentResponse createComment(String issueId, CommentCreateRequest req, User author) {
        IssueComment c = new IssueComment();
        c.setIssueId(issueId);
        c.setAuthorId(author != null ? author.getId() : null);
        c.setMessage(req.message);
        c.setCreatedAt(Instant.now());
        IssueComment saved = commentRepo.save(c);

        // add event
        IssueEvent ev = new IssueEvent();
        ev.setIssueId(issueId);
        ev.setType("COMMENT");
        ev.setActorId(author != null ? author.getId() : null);
        ev.setNote(req.message);
        ev.setCreatedAt(Instant.now());
        eventRepo.save(ev);

        // map to DTO
        CommentResponse r = new CommentResponse();
        r.id = saved.getId();
        r.issueId = saved.getIssueId();
        r.authorId = saved.getAuthorId();
        r.message = saved.getMessage();
        r.createdAt = saved.getCreatedAt();

        // try to set author name from provided author object (or user repo)
        if (author != null) {
            r.authorName = author.getName();
        } else if (r.authorId != null) {
            userRepository.findById(r.authorId).ifPresent(u -> r.authorName = u.getName());
        }
        return r;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> listComments(String issueId) {
        return commentRepo.findByIssueIdOrderByCreatedAtAsc(issueId).stream()
                .map(c -> {
                    CommentResponse r = new CommentResponse();
                    r.id = c.getId();
                    r.issueId = c.getIssueId();
                    r.authorId = c.getAuthorId();
                    r.message = c.getMessage();
                    r.createdAt = c.getCreatedAt();
                    // try to fill name from user repo (optional)
                    if (r.authorId != null) {
                        userRepository.findById(r.authorId).ifPresent(u -> r.authorName = u.getName());
                    }
                    return r;
                })
                .collect(Collectors.toList());
    }
}



