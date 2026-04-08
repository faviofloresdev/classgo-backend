package com.classgo.backend.api.learning;

import com.classgo.backend.api.learning.dto.LearningDtos.CreateTopicRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.TopicResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.UpdateTopicRequest;
import com.classgo.backend.application.learning.LearningPlatformService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/topics")
public class LearningTopicController {

    private final LearningPlatformService service;

    public LearningTopicController(LearningPlatformService service) {
        this.service = service;
    }

    @PostMapping
    public TopicResponse createTopic(@Valid @RequestBody CreateTopicRequest request) {
        return service.createTopic(request);
    }

    @PatchMapping("/{topicId}")
    public TopicResponse updateTopic(@PathVariable UUID topicId, @RequestBody UpdateTopicRequest request) {
        return service.updateTopic(topicId, request);
    }

    @DeleteMapping("/{topicId}")
    public void deleteTopic(@PathVariable UUID topicId) {
        service.deleteTopic(topicId);
    }

    @GetMapping("/{topicId}")
    public TopicResponse topic(@PathVariable UUID topicId) {
        return service.topic(topicId);
    }

    @GetMapping("/teachers/me")
    public List<TopicResponse> teacherTopics() {
        return service.teacherTopics();
    }
}
