// CommentService.java
package com.publicissuetracker.service;

import com.publicissuetracker.model.IssueComment;
import java.util.List;

public interface CommentService {
    IssueComment addComment(String issueId, String authorId, String message);
    List<IssueComment> listComments(String issueId);
}

