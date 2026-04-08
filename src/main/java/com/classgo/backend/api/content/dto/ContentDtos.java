package com.classgo.backend.api.content.dto;

import com.classgo.backend.domain.enums.QuestionType;
import java.util.List;
import java.util.UUID;

public final class ContentDtos {
    private ContentDtos() {}

    public record SubjectResponse(UUID id, String name) {}
    public record TopicResponse(UUID id, UUID subjectId, String grade, String title, String description) {}
    public record QuestionOptionResponse(UUID id, String optionText, Integer sortOrder) {}
    public record QuestionResponse(UUID id, QuestionType type, String prompt, String explanation, Integer difficultyLevel,
                                   Integer sortOrder, List<QuestionOptionResponse> options) {}
}
