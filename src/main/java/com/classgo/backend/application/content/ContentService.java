package com.classgo.backend.application.content;

import com.classgo.backend.api.content.dto.ContentDtos.QuestionOptionResponse;
import com.classgo.backend.api.content.dto.ContentDtos.QuestionResponse;
import com.classgo.backend.api.content.dto.ContentDtos.SubjectResponse;
import com.classgo.backend.api.content.dto.ContentDtos.TopicResponse;
import com.classgo.backend.domain.repository.QuestionOptionRepository;
import com.classgo.backend.domain.repository.QuestionRepository;
import com.classgo.backend.domain.repository.SubjectRepository;
import com.classgo.backend.domain.repository.TopicRepository;
import com.classgo.backend.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ContentService {

    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;

    public ContentService(
        SubjectRepository subjectRepository,
        TopicRepository topicRepository,
        QuestionRepository questionRepository,
        QuestionOptionRepository questionOptionRepository
    ) {
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
    }

    public List<SubjectResponse> subjects() {
        return subjectRepository.findAll().stream().map(subject -> new SubjectResponse(subject.getId(), subject.getName())).toList();
    }

    public List<TopicResponse> topics(UUID subjectId, String grade) {
        return topicRepository.findBySubjectIdAndGrade(subjectId, grade).stream()
            .map(topic -> new TopicResponse(topic.getId(), topic.getSubject().getId(), topic.getGrade(), topic.getTitle(), topic.getDescription()))
            .toList();
    }

    public List<QuestionResponse> questions(UUID topicId) {
        topicRepository.findById(topicId).orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
        return questionRepository.findByTopicIdOrderBySortOrderAsc(topicId).stream().map(question -> new QuestionResponse(
            question.getId(),
            question.getType(),
            question.getPrompt(),
            question.getExplanation(),
            question.getDifficultyLevel(),
            question.getSortOrder(),
            questionOptionRepository.findByQuestionIdOrderBySortOrderAsc(question.getId()).stream()
                .map(option -> new QuestionOptionResponse(option.getId(), option.getOptionText(), option.getSortOrder()))
                .toList()
        )).toList();
    }
}
