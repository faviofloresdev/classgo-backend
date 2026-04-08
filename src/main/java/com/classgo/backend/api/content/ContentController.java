package com.classgo.backend.api.content;

import com.classgo.backend.api.content.dto.ContentDtos.QuestionResponse;
import com.classgo.backend.api.content.dto.ContentDtos.SubjectResponse;
import com.classgo.backend.api.content.dto.ContentDtos.TopicResponse;
import com.classgo.backend.application.content.ContentService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/subjects")
    public List<SubjectResponse> subjects() {
        return contentService.subjects();
    }

    @GetMapping("/topics")
    public List<TopicResponse> topics(@RequestParam UUID subjectId, @RequestParam String grade) {
        return contentService.topics(subjectId, grade);
    }

    @GetMapping("/topics/{id}/questions")
    public List<QuestionResponse> questions(@PathVariable UUID id) {
        return contentService.questions(id);
    }
}
