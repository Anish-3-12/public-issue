package com.publicissuetracker.repository;

import com.publicissuetracker.model.IssueEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IssueEventRepository extends JpaRepository<IssueEvent, String> {
    List<IssueEvent> findByIssueIdOrderByCreatedAtAsc(String issueId);
}

