// CommentServiceImpl.java
package com.publicissuetracker.service.impl;

import com.publicissuetracker.model.IssueComment;
import com.publicissuetracker.model.IssueEvent;
import com.publicissuetracker.repository.IssueCommentRepository;
import com.publicissuetracker.repository.IssueEventRepository;
import com.publicissuetracker.service.CommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    private final IssueCommentRepository commentRepo;
    private final IssueEventRepository eventRepo;

    public CommentServiceImpl(IssueCommentRepository commentRepo, IssueEventRepository eventRepo) {
        this.commentRepo = commentRepo;
        this.eventRepo = eventRepo;
    }

    @Override
    @Transactional
    public IssueComment addComment(String issueId, String authorId, String message) {
        IssueComment c = new IssueComment();
        c.setIssueId(issueId);
        c.setAuthorId(authorId);
        c.setMessage(message);
        IssueComment saved = commentRepo.save(c);

        IssueEvent ev = new IssueEvent();
        ev.setIssueId(issueId);
        ev.setType("COMMENT");
        ev.setActorId(authorId);
        ev.setNote(message);
        eventRepo.save(ev);

        return saved;
    }

    @Override
    public List<IssueComment> listComments(String issueId) {
        return commentRepo.findByIssueIdOrderByCreatedAtAsc(issueId);
    }
}

