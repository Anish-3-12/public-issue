package com.publicissuetracker.repository;

import com.publicissuetracker.model.IssueComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IssueCommentRepository extends JpaRepository<IssueComment, String> {
    List<IssueComment> findByIssueIdOrderByCreatedAtAsc(String issueId);
}

