package com.publicissuetracker.service.impl;

import com.publicissuetracker.dto.CommentCreateRequest;
import com.publicissuetracker.dto.CommentResponse;
import com.publicissuetracker.model.Issue;
import com.publicissuetracker.model.IssueComment;
import com.publicissuetracker.model.IssueEvent;
import com.publicissuetracker.model.User;
import com.publicissuetracker.repository.IssueCommentRepository;
import com.publicissuetracker.repository.IssueEventRepository;
import com.publicissuetracker.repository.IssueRepository;
import com.publicissuetracker.repository.UserRepository;
import com.publicissuetracker.service.CommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final IssueCommentRepository commentRepo;
    private final IssueEventRepository eventRepo;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    public CommentServiceImpl(IssueCommentRepository commentRepo,
                              IssueEventRepository eventRepo,
                              IssueRepository issueRepository,
                              UserRepository userRepository) {
        this.commentRepo = commentRepo;
        this.eventRepo = eventRepo;
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CommentResponse createComment(String issueId, CommentCreateRequest req, User author) {
        // validate issue exists
        Optional<Issue> issueOpt = issueRepository.findById(issueId);
        if (issueOpt.isEmpty()) {
            throw new IllegalArgumentException("Issue not found: " + issueId);
        }

        // validate text
        String text = req == null ? null : req.getText();
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment text must not be empty");
        }

        // create and save IssueComment
        IssueComment c = new IssueComment();
        c.setIssueId(issueId);
        c.setAuthorId(author != null ? author.getId() : null);
        c.setMessage(text);
        c.setCreatedAt(Instant.now());
        IssueComment saved = commentRepo.save(c);

        // create and save an IssueEvent
        IssueEvent ev = new IssueEvent();
        ev.setIssueId(issueId);
        ev.setType("COMMENT");
        ev.setActorId(author != null ? author.getId() : null);
        ev.setNote(text);
        ev.setCreatedAt(Instant.now());
        eventRepo.save(ev);

        // update issue's updatedAt
        Issue issue = issueOpt.get();
        issue.setUpdatedAt(Instant.now());
        issueRepository.save(issue);

        // build response DTO (include authorName if possible)
        CommentResponse resp = new CommentResponse();
        resp.id = saved.getId();
        resp.issueId = saved.getIssueId();
        resp.authorId = saved.getAuthorId();
        resp.message = saved.getMessage();
        resp.createdAt = saved.getCreatedAt();
        if (author != null) resp.authorName = author.getName();

        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> listComments(String issueId) {
        List<IssueComment> comments = commentRepo.findByIssueIdOrderByCreatedAtAsc(issueId);
        if (comments.isEmpty()) return Collections.emptyList();

        // gather authorIds (unique)
        Set<String> authorIds = comments.stream()
                .map(IssueComment::getAuthorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // final map reference so it can be safely captured by lambda
        final Map<String, User> usersById = new HashMap<>();

        if (!authorIds.isEmpty()) {
            Iterable<User> users = userRepository.findAllById(authorIds);
            for (User u : users) {
                if (u != null && u.getId() != null) {
                    usersById.put(u.getId(), u);
                }
            }
        }

        return comments.stream().map(c -> {
            CommentResponse resp = new CommentResponse();
            resp.id = c.getId();
            resp.issueId = c.getIssueId();
            resp.authorId = c.getAuthorId();
            resp.message = c.getMessage();
            resp.createdAt = c.getCreatedAt();

            // resolve name from cached map if available
            if (c.getAuthorId() != null && usersById.containsKey(c.getAuthorId())) {
                resp.authorName = usersById.get(c.getAuthorId()).getName();
            } else {
                resp.authorName = null;
            }
            return resp;
        }).collect(Collectors.toList());
    }
}
