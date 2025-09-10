package com.publicissuetracker.api;

import com.publicissuetracker.model.IssueEvent;
import com.publicissuetracker.repository.IssueEventRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/issues/{id}/events")
public class TimelineController {
    private final IssueEventRepository eventRepo;
    public TimelineController(IssueEventRepository eventRepo){ this.eventRepo = eventRepo; }

    @GetMapping
    public List<IssueEvent> list(@PathVariable("id") String id){
        return eventRepo.findByIssueIdOrderByCreatedAtAsc(id);
    }
}

