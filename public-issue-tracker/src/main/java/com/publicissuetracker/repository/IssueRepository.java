package com.publicissuetracker.repository;

import com.publicissuetracker.model.Issue;
import com.publicissuetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, String> {

    // find all issues created by a specific user
    List<Issue> findByCreatedBy(User createdBy);

    // find all issues assigned to a specific admin
    List<Issue> findByAssignedTo(User assignedTo);

    // find issues by status
    List<Issue> findByStatus(String status);

    // later we can add filters like category, location, etc.
}

