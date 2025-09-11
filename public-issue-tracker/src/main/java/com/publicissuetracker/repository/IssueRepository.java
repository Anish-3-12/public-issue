package com.publicissuetracker.repository;

import com.publicissuetracker.model.Issue;
import com.publicissuetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, String> {

    // find all issues created by a specific user
    List<Issue> findByCreatedBy(User createdBy);

    // find all issues assigned to a specific admin
    List<Issue> findByAssignedTo(User assignedTo);

    // find issues by status
    List<Issue> findByStatus(String status);

    long countByStatus(String status);

    /**
     * Return issues for a reporter (most recent first).
     * Using an explicit JPQL query because the Issue entity's field is 'createdBy' (User).
     * We query by createdBy.id to match the reporterId parameter.
     */
    @Query("SELECT i FROM Issue i WHERE i.createdBy.id = :reporterId ORDER BY i.createdAt DESC")
    List<Issue> findByReporterIdOrderByCreatedAtDesc(@Param("reporterId") String reporterId);

    // later we can add filters like category, location, etc.
}


