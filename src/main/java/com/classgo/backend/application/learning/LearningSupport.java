package com.classgo.backend.application.learning;

import com.classgo.backend.api.learning.dto.LearningDtos.AuthUserResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.AchievementUpdateResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.AvatarResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.BasicUserResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.ClassroomResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.ClassroomWithDetailsResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.GameplayClassroomResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.GameplayTopicResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.HistoryEntryResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.InAppNotificationResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.LeaderboardEntryResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.PlanResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.PlanTopicResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.StudentResultResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.StudentResultWithDetailsResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.TeacherResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.TeacherResultRowResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.TopicLiteResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.TopicResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.TopicSummaryResponse;
import com.classgo.backend.domain.enums.UserRole;
import com.classgo.backend.domain.model.AppClassroom;
import com.classgo.backend.domain.model.AvatarCatalog;
import com.classgo.backend.domain.model.LearningTopic;
import com.classgo.backend.domain.model.Notification;
import com.classgo.backend.domain.model.PlanTopic;
import com.classgo.backend.domain.model.StudentAttempt;
import com.classgo.backend.domain.model.StudyPlan;
import com.classgo.backend.domain.model.User;
import com.classgo.backend.infrastructure.security.SecurityUtils;
import com.classgo.backend.shared.exception.BusinessRuleViolationException;
import com.classgo.backend.shared.exception.ResourceNotFoundException;
import com.classgo.backend.shared.exception.UnauthorizedOperationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class LearningSupport {

    private final ObjectMapper objectMapper;

    public LearningSupport(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void requireTeacher() {
        if (SecurityUtils.currentUser().role() != UserRole.TEACHER) {
            throw new UnauthorizedOperationException("FORBIDDEN", "Only teachers can perform this action");
        }
    }

    public void requireStudent() {
        if (SecurityUtils.currentUser().role() != UserRole.STUDENT) {
            throw new UnauthorizedOperationException("FORBIDDEN", "Only students can perform this action");
        }
    }

    public JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("No se pudo procesar JSON");
        }
    }

    public String writeJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("No se pudo serializar JSON");
        }
    }

    public JsonNode normalizeAndValidateQuestions(JsonNode questions) {
        if (questions == null || !questions.isArray() || questions.isEmpty()) {
            throw new BusinessRuleViolationException("TOPIC_QUESTIONS_REQUIRED", "The topic must have at least one question");
        }
        ArrayNode normalizedQuestions = objectMapper.createArrayNode();
        for (JsonNode questionNode : questions) {
            if (!(questionNode instanceof ObjectNode question)) {
                throw new BusinessRuleViolationException("VALIDATION_ERROR", "Each question must be a JSON object");
            }
            requiredText(question, "id", "Each question must have an id");
            String type = requiredText(question, "type", "Each question must have a type");
            requiredText(question, "prompt", "Each question must have a prompt");
            String normalizedType = normalizeQuestionType(type);
            question.put("type", normalizedType);
            switch (normalizedType) {
                case "single_choice" -> validateChoiceQuestion(question, true, false, false);
                case "multiple_choice" -> validateChoiceQuestion(question, false, false, false);
                case "listen_and_select_text" -> validateChoiceQuestion(question, true, true, false);
                case "listen_and_select_image" -> validateChoiceQuestion(question, true, true, true);
                case "image_selection" -> validateChoiceQuestion(question, true, false, true);
                case "image_multiple_selection" -> validateChoiceQuestion(question, false, false, true);
                case "fill_in_blank" -> validateFillBlank(question);
                case "match_items" -> validateMatchItems(question);
                case "single_text_ordering" -> validateOrderingQuestion(question, false, false);
                case "phrase_ordering" -> validateOrderingQuestion(question, false, false);
                case "image_ordering" -> validateOrderingQuestion(question, true, true);
                default -> throw new BusinessRuleViolationException("QUESTION_TYPE_INVALID", "Unsupported question type: " + type);
            }
            normalizedQuestions.add(question);
        }
        return normalizedQuestions;
    }

    private void validateChoiceQuestion(JsonNode question, boolean exactlyOneCorrect, boolean audioRequired, boolean imageRequired) {
        JsonNode options = question.get("options");
        if (options == null || !options.isArray() || options.size() < 2) {
            throw new BusinessRuleViolationException("QUESTION_OPTIONS_INVALID", "The question must have at least 2 options");
        }
        int correctCount = 0;
        for (JsonNode option : options) {
            requiredText(option, "id", "Each option must have an id");
            if (imageRequired) {
                requiredText(option, "imageUrl", "Each option must have an imageUrl");
            } else {
                requiredText(option, "text", "Each option must have text");
            }
            if (option.path("isCorrect").asBoolean(false)) {
                correctCount++;
            }
        }
        if (exactlyOneCorrect && correctCount != 1) {
            throw new BusinessRuleViolationException("QUESTION_CORRECT_OPTIONS_INVALID", "The question must have exactly one correct option");
        }
        if (!exactlyOneCorrect && correctCount < 1) {
            throw new BusinessRuleViolationException("QUESTION_CORRECT_OPTIONS_INVALID", "The question must have at least one correct option");
        }
        if (audioRequired) {
            requiredText(question, "audioText", "listen_and_select requires audioText");
        }
    }

    private void validateFillBlank(JsonNode question) {
        String word = requiredText(question, "word", "fill_in_blank requires word");
        JsonNode hiddenIndexes = question.get("hiddenIndexes");
        if (hiddenIndexes == null || !hiddenIndexes.isArray() || hiddenIndexes.isEmpty()) {
            throw new BusinessRuleViolationException("FILL_IN_BLANK_INDEXES_INVALID", "fill_in_blank requires hiddenIndexes");
        }
        List<Integer> seen = new ArrayList<>();
        for (JsonNode indexNode : hiddenIndexes) {
            int index = indexNode.asInt(-1);
            if (index < 0 || index >= word.length() || seen.contains(index)) {
                throw new BusinessRuleViolationException("FILL_IN_BLANK_INDEXES_INVALID", "hiddenIndexes contains invalid or duplicate values");
            }
            seen.add(index);
        }
    }

    private void validateMatchItems(JsonNode question) {
        requiredText(question, "instruction", "match_items requires instruction");
        JsonNode pairs = question.get("pairs");
        if (pairs == null || !pairs.isArray() || pairs.size() < 2) {
            throw new BusinessRuleViolationException("MATCH_ITEMS_INVALID", "match_items requires at least 2 pairs");
        }
        for (JsonNode pair : pairs) {
            requiredText(pair, "left", "Each pair must have left");
            String right = requiredText(pair, "right", "Each pair must have right");
            if (right.contains(" ")) {
                throw new BusinessRuleViolationException("MATCH_ITEMS_RIGHT_INVALID", "right must be a single word");
            }
        }
    }

    private void validateOrderingQuestion(JsonNode question, boolean imageRequired, boolean imageCaptionOptional) {
        JsonNode items = question.get("items");
        if (items == null || !items.isArray() || items.size() < 2) {
            throw new BusinessRuleViolationException("QUESTION_ITEMS_INVALID", "The question must have at least 2 items");
        }
        for (JsonNode item : items) {
            requiredText(item, "id", "Each item must have an id");
            if (imageRequired) {
                requiredText(item, "imageUrl", "Each item must have an imageUrl");
                if (!imageCaptionOptional && item.path("text").asText("").isBlank()) {
                    throw new BusinessRuleViolationException("QUESTION_ITEMS_INVALID", "Each item must have text");
                }
            } else {
                requiredText(item, "text", "Each item must have text");
            }
        }
    }

    private String normalizeQuestionType(String type) {
        return switch (type) {
            case "listen_and_select" -> "listen_and_select_text";
            case "text_ordering" -> "phrase_ordering";
            case "single_choice", "multiple_choice", "fill_in_blank", "match_items",
                "listen_and_select_text", "listen_and_select_image",
                "image_selection", "image_multiple_selection",
                "single_text_ordering", "phrase_ordering", "image_ordering" -> type;
            default -> type;
        };
    }

    private String requiredText(JsonNode node, String field, String message) {
        String value = node.path(field).asText(null);
        if (value == null || value.isBlank()) {
            throw new BusinessRuleViolationException("VALIDATION_ERROR", message);
        }
        return value;
    }

    public ClassroomResponse classroomResponse(AppClassroom classroom) {
        return new ClassroomResponse(
            classroom.getId(),
            classroom.getName(),
            classroom.getCode(),
            classroom.getDescription(),
            classroom.getTeacher().getId(),
            classroom.getActivePlan() != null ? classroom.getActivePlan().getId() : null,
            classroom.getCurrentWeek(),
            classroom.getCreatedAt()
        );
    }

    public TeacherResponse teacherResponse(User user) {
        return new TeacherResponse(user.getId(), user.getName(), user.getAvatarId());
    }

    public BasicUserResponse basicUserResponse(User user) {
        return new BasicUserResponse(user.getId(), user.getName(), user.getAvatarId());
    }

    public AuthUserResponse authUserResponse(User user) {
        return authUserResponse(user, null);
    }

    public AuthUserResponse authUserResponse(User user, AchievementUpdateResponse achievements) {
        return new AuthUserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getAvatarId(),
            user.getRole() == UserRole.STUDENT ? user.getAvatarId() : null,
            user.getRole() == UserRole.STUDENT ? user.getParentAvatarId() : null,
            achievements
        );
    }

    public AvatarResponse avatarResponse(AvatarCatalog avatar) {
        return new AvatarResponse(avatar.getId(), avatar.getName(), avatar.getCategory(), avatar.getUrl());
    }

    public TopicResponse topicResponse(LearningTopic topic) {
        return new TopicResponse(
            topic.getId(),
            topic.getName(),
            topic.getDescription(),
            topic.getIcon(),
            topic.getColor(),
            topic.getDifficulty(),
            parseJson(topic.getQuestionsJson()),
            topic.getTeacher().getId(),
            topic.getCreatedAt()
        );
    }

    public PlanResponse planResponse(StudyPlan plan, List<PlanTopic> planTopics) {
        List<PlanTopicResponse> topics = planTopics.stream()
            .sorted(Comparator.comparingInt(PlanTopic::getWeekNumber))
            .map(this::planTopicResponse)
            .toList();
        return new PlanResponse(
            plan.getId(),
            plan.getName(),
            plan.getDescription(),
            plan.getTeacher().getId(),
            plan.getActivationMode(),
            plan.getStartDate(),
            plan.getCreatedAt(),
            topics
        );
    }

    public PlanTopicResponse planTopicResponse(PlanTopic planTopic) {
        LearningTopic topic = planTopic.getTopic();
        return new PlanTopicResponse(
            planTopic.getId(),
            planTopic.getPlan().getId(),
            topic.getId(),
            planTopic.getWeekNumber(),
            planTopic.isActive(),
            new TopicSummaryResponse(topic.getId(), topic.getName(), topic.getColor(), parseJson(topic.getQuestionsJson()))
        );
    }

    public ClassroomWithDetailsResponse classroomWithDetails(
        AppClassroom classroom,
        List<User> students,
        StudyPlan plan,
        List<PlanTopic> planTopics
    ) {
        return new ClassroomWithDetailsResponse(
            classroom.getId(),
            classroom.getName(),
            classroom.getCode(),
            classroom.getDescription(),
            classroom.getTeacher().getId(),
            classroom.getActivePlan() != null ? classroom.getActivePlan().getId() : null,
            classroom.getCurrentWeek(),
            classroom.getCreatedAt(),
            teacherResponse(classroom.getTeacher()),
            students.stream().map(this::basicUserResponse).toList(),
            plan != null ? planResponse(plan, planTopics) : null
        );
    }

    public GameplayClassroomResponse gameplayClassroomResponse(AppClassroom classroom) {
        return new GameplayClassroomResponse(classroom.getId(), classroom.getName(), classroom.getCurrentWeek());
    }

    public GameplayTopicResponse gameplayTopicResponse(LearningTopic topic) {
        return new GameplayTopicResponse(topic.getId(), topic.getName(), topic.getDescription(), topic.getColor(), parseJson(topic.getQuestionsJson()));
    }

    public InAppNotificationResponse inAppNotificationResponse(Notification notification) {
        JsonNode payload = parseJson(notification.getPayload());
        return new InAppNotificationResponse(
            notification.getId(),
            notification.getType(),
            payload.path("message").asText(""),
            payload,
            notification.getCreatedAt(),
            notification.getReadAt()
        );
    }

    public StudentResultResponse resultResponse(StudentAttempt attempt) {
        return resultResponse(attempt, null);
    }

    public StudentResultResponse resultResponse(StudentAttempt attempt, AchievementUpdateResponse achievements) {
        return new StudentResultResponse(
            attempt.getId(),
            attempt.getStudent().getId(),
            attempt.getClassroom().getId(),
            attempt.getTopic().getId(),
            attempt.getWeekNumber(),
            attempt.getScore(),
            attempt.getCompletedAt(),
            attempt.getTimeSpent(),
            attempt.getCorrectAnswers(),
            attempt.getTotalQuestions(),
            parseJson(attempt.getAnswersJson()),
            achievements
        );
    }

    public StudentResultWithDetailsResponse resultWithDetails(StudentAttempt attempt) {
        return new StudentResultWithDetailsResponse(
            attempt.getId(),
            attempt.getStudent().getId(),
            attempt.getClassroom().getId(),
            attempt.getTopic().getId(),
            attempt.getWeekNumber(),
            attempt.getScore(),
            attempt.getCompletedAt(),
            attempt.getTimeSpent(),
            attempt.getCorrectAnswers(),
            attempt.getTotalQuestions(),
            parseJson(attempt.getAnswersJson()),
            basicUserResponse(attempt.getStudent()),
            new TopicLiteResponse(attempt.getTopic().getId(), attempt.getTopic().getName(), attempt.getTopic().getColor())
        );
    }

    public TeacherResultRowResponse teacherResultRow(StudentAttempt attempt) {
        return new TeacherResultRowResponse(
            attempt.getStudent().getId(),
            attempt.getWeekNumber(),
            attempt.getScore(),
            attempt.getTimeSpent(),
            basicUserResponse(attempt.getStudent())
        );
    }

    public LeaderboardEntryResponse leaderboardEntry(User student, int totalScore, int rank) {
        return new LeaderboardEntryResponse(basicUserResponse(student), totalScore, rank);
    }

    public HistoryEntryResponse historyEntry(StudentAttempt attempt) {
        return new HistoryEntryResponse(
            attempt.getWeekNumber(),
            attempt.getTopic().getId(),
            attempt.getTopic().getName(),
            attempt.getScore(),
            attempt.getTimeSpent(),
            attempt.getCompletedAt()
        );
    }

    public User requireUser(UUID userId, com.classgo.backend.domain.repository.UserRepository userRepository) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));
    }
}
