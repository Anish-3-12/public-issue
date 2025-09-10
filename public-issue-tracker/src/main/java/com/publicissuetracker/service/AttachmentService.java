package com.publicissuetracker.service;

import com.publicissuetracker.model.Issue;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {
    void uploadAttachments(Issue issue, List<MultipartFile> files);

    List<String> listAttachments(Issue issue);
}

