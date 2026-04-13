package com.classgo.backend.application.learning;

import com.classgo.backend.api.learning.dto.LearningDtos.ClassroomPedagogicalTagInsightsResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.CreatePedagogicalTagRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.PedagogicalTagMetricResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.PedagogicalTagResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.StudentPedagogicalTagInsightsResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.UpdatePedagogicalTagRequest;
import com.classgo.backend.domain.enums.UserRole;
import com.classgo.backend.domain.model.AppClassroom;
import com.classgo.backend.domain.model.LearningTopic;
import com.classgo.backend.domain.model.PedagogicalTag;
import com.classgo.backend.domain.model.StudentAttempt;
import com.classgo.backend.domain.model.User;
import com.classgo.backend.domain.repository.AppClassroomRepository;
import com.classgo.backend.domain.repository.EnrollmentRepository;
import com.classgo.backend.domain.repository.LearningTopicRepository;
import com.classgo.backend.domain.repository.PedagogicalTagRepository;
import com.classgo.backend.domain.repository.StudentAttemptRepository;
import com.classgo.backend.domain.repository.UserRepository;
import com.classgo.backend.infrastructure.security.SecurityUtils;
import com.classgo.backend.shared.exception.BusinessRuleViolationException;
import com.classgo.backend.shared.exception.DuplicateResourceException;
import com.classgo.backend.shared.exception.ResourceNotFoundException;
import com.classgo.backend.shared.exception.UnauthorizedOperationException;
import com.fasterxml.jackson.databind.JsonNode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedagogicalTagService {

    private final PedagogicalTagRepository pedagogicalTagRepository;
    private final LearningTopicRepository learningTopicRepository;
    private final StudentAttemptRepository studentAttemptRepository;
    private final AppClassroomRepository classroomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final LearningSupport support;

    public PedagogicalTagService(
        PedagogicalTagRepository pedagogicalTagRepository,
        LearningTopicRepository learningTopicRepository,
        StudentAttemptRepository studentAttemptRepository,
        AppClassroomRepository classroomRepository,
        EnrollmentRepository enrollmentRepository,
        UserRepository userRepository,
        LearningSupport support
    ) {
        this.pedagogicalTagRepository = pedagogicalTagRepository;
        this.learningTopicRepository = learningTopicRepository;
        this.studentAttemptRepository = studentAttemptRepository;
        this.classroomRepository = classroomRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.support = support;
    }

    @Transactional
    public PedagogicalTagResponse create(CreatePedagogicalTagRequest request) {
        support.requireTeacher();
        User teacher = currentUser();
        String normalizedName = request.name().trim();
        String slug = slugify(normalizedName);
        if (slug.isBlank()) {
            throw new BusinessRuleViolationException("PEDAGOGICAL_TAG_INVALID", "The pedagogical tag name is invalid");
        }
        if (pedagogicalTagRepository.existsByTeacherIdAndSlug(teacher.getId(), slug)) {
            throw new DuplicateResourceException("PEDAGOGICAL_TAG_ALREADY_EXISTS", "The pedagogical tag already exists");
        }
        PedagogicalTag tag = new PedagogicalTag();
        tag.setTeacher(teacher);
        tag.setName(normalizedName);
        tag.setSlug(slug);
        return response(pedagogicalTagRepository.save(tag));
    }

    @Transactional(readOnly = true)
    public List<PedagogicalTagResponse> list(String query) {
        support.requireTeacher();
        List<PedagogicalTag> tags = query == null || query.isBlank()
            ? pedagogicalTagRepository.findAllByTeacherIdOrderByNameAsc(SecurityUtils.currentUserId())
            : pedagogicalTagRepository.findAllByTeacherIdAndNameContainingIgnoreCaseOrderByNameAsc(SecurityUtils.currentUserId(), query.trim());
        return tags.stream().map(this::response).toList();
    }

    @Transactional
    public PedagogicalTagResponse update(UUID tagId, UpdatePedagogicalTagRequest request) {
        support.requireTeacher();
        PedagogicalTag tag = ownedTag(tagId);
        tag.setName(request.name().trim());
        return response(pedagogicalTagRepository.save(tag));
    }

    @Transactional
    public void delete(UUID tagId) {
        support.requireTeacher();
        PedagogicalTag tag = ownedTag(tagId);
        if (isTagInUse(SecurityUtils.currentUserId(), tag.getSlug())) {
            throw new BusinessRuleViolationException("PEDAGOGICAL_TAG_IN_USE", "The pedagogical tag cannot be deleted because it is already used by questions");
        }
        pedagogicalTagRepository.delete(tag);
    }

    @Transactional(readOnly = true)
    public Set<String> allowedTagSlugs(UUID teacherId) {
        return pedagogicalTagRepository.findAllByTeacherIdOrderByNameAsc(teacherId).stream()
            .map(PedagogicalTag::getSlug)
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    @Transactional(readOnly = true)
    public ClassroomPedagogicalTagInsightsResponse classroomInsights(UUID classroomId) {
        support.requireTeacher();
        AppClassroom classroom = ownedClassroom(classroomId);
        List<StudentAttempt> attempts = latestAttempts(studentAttemptRepository.findAllByClassroomIdOrderByWeekNumberAscCompletedAtAsc(classroomId));
        return new ClassroomPedagogicalTagInsightsResponse(
            classroom.getId(),
            classroom.getName(),
            aggregateTagMetrics(SecurityUtils.currentUserId(), attempts)
        );
    }

    @Transactional(readOnly = true)
    public StudentPedagogicalTagInsightsResponse studentInsights(UUID classroomId, UUID studentId) {
        support.requireTeacher();
        ownedClassroom(classroomId);
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));
        if (student.getRole() != UserRole.STUDENT || !enrollmentRepository.existsByClassroomIdAndStudentId(classroomId, studentId)) {
            throw new ResourceNotFoundException("ENROLLMENT_NOT_FOUND", "The student does not belong to the classroom");
        }
        List<StudentAttempt> attempts = latestAttempts(studentAttemptRepository.findAllByClassroomIdAndStudentIdOrderByWeekNumberAscCompletedAtAsc(classroomId, studentId));
        return new StudentPedagogicalTagInsightsResponse(studentId, student.getName(), aggregateTagMetrics(SecurityUtils.currentUserId(), attempts));
    }

    private List<PedagogicalTagMetricResponse> aggregateTagMetrics(UUID teacherId, List<StudentAttempt> attempts) {
        Map<String, PedagogicalTagAccumulator> bySlug = new LinkedHashMap<>();
        List<LearningTopic> teacherTopics = learningTopicRepository.findAllByTeacherIdOrderByCreatedAtDesc(teacherId);
        Map<String, String> namesBySlug = pedagogicalTagRepository.findAllByTeacherIdOrderByNameAsc(teacherId).stream()
            .collect(java.util.stream.Collectors.toMap(PedagogicalTag::getSlug, PedagogicalTag::getName, (left, right) -> left, LinkedHashMap::new));
        Map<UUID, List<QuestionDescriptor>> questionsByTopicId = new LinkedHashMap<>();
        for (LearningTopic topic : teacherTopics) {
            questionsByTopicId.put(topic.getId(), questionDescriptors(topic));
        }

        for (StudentAttempt attempt : attempts) {
            List<QuestionDescriptor> questions = questionsByTopicId.getOrDefault(attempt.getTopic().getId(), List.of());
            JsonNode answers = support.parseJson(attempt.getAnswersJson());
            Set<String> attemptTags = new LinkedHashSet<>();
            long mappedAnswers = 0;
            if (answers.isArray()) {
                int answerIndex = 0;
                for (JsonNode answer : answers) {
                    QuestionDescriptor question = resolveQuestion(answer, questions, answerIndex++);
                    if (question == null) {
                        continue;
                    }
                    boolean hasCorrect = answer.has("correct");
                    boolean correct = answer.path("correct").asBoolean(false);
                    for (String tag : question.tags()) {
                        PedagogicalTagAccumulator metric = bySlug.computeIfAbsent(tag, ignored -> new PedagogicalTagAccumulator());
                        metric.answeredCount++;
                        if (hasCorrect && correct) {
                            metric.correctCount++;
                        }
                        attemptTags.add(tag);
                    }
                    if (!question.tags().isEmpty()) {
                        mappedAnswers++;
                    }
                }
            }
            if (mappedAnswers == 0) {
                Set<String> distinctTopicTags = distinctTopicTags(questions);
                if (distinctTopicTags.size() == 1) {
                    String onlyTag = distinctTopicTags.iterator().next();
                    PedagogicalTagAccumulator metric = bySlug.computeIfAbsent(onlyTag, ignored -> new PedagogicalTagAccumulator());
                    long answeredFallback = answers.isArray() && answers.size() > 0 ? answers.size() : Math.max(attempt.getTotalQuestions(), 0);
                    metric.answeredCount += answeredFallback;
                    metric.correctCount += Math.min(Math.max(attempt.getCorrectAnswers(), 0), answeredFallback);
                    attemptTags.add(onlyTag);
                }
            }
            for (String tag : attemptTags) {
                PedagogicalTagAccumulator metric = bySlug.computeIfAbsent(tag, ignored -> new PedagogicalTagAccumulator());
                metric.attemptCount++;
                metric.totalScore += attempt.getScore();
            }
        }

        for (List<QuestionDescriptor> questions : questionsByTopicId.values()) {
            for (QuestionDescriptor question : questions) {
                for (String tag : question.tags()) {
                    bySlug.computeIfAbsent(tag, ignored -> new PedagogicalTagAccumulator()).questionCount++;
                }
            }
        }

        return bySlug.entrySet().stream()
            .map(entry -> {
                PedagogicalTagAccumulator metric = entry.getValue();
                double accuracy = metric.answeredCount == 0 ? 0D : (double) metric.correctCount / metric.answeredCount;
                int averageScore = metric.attemptCount == 0 ? 0 : (int) Math.round((double) metric.totalScore / metric.attemptCount);
                return new PedagogicalTagMetricResponse(
                    entry.getKey(),
                    namesBySlug.getOrDefault(entry.getKey(), entry.getKey()),
                    metric.questionCount,
                    metric.answeredCount,
                    metric.correctCount,
                    accuracy,
                    averageScore
                );
            })
            .sorted(Comparator.comparing(PedagogicalTagMetricResponse::name))
            .toList();
    }

    private List<QuestionDescriptor> questionDescriptors(LearningTopic topic) {
        List<QuestionDescriptor> result = new ArrayList<>();
        JsonNode questions = support.parseJson(topic.getQuestionsJson());
        if (!questions.isArray()) {
            return result;
        }
        for (JsonNode question : questions) {
            String questionId = question.path("id").asText(null);
            if (questionId == null || questionId.isBlank()) {
                continue;
            }
            Set<String> tags = new LinkedHashSet<>();
            JsonNode pedagogicalTags = question.get("pedagogicalTags");
            if (pedagogicalTags != null && pedagogicalTags.isArray()) {
                for (JsonNode tag : pedagogicalTags) {
                    String slug = tag.asText(null);
                    if (slug != null && !slug.isBlank()) {
                        tags.add(slug);
                    }
                }
            }
            result.add(new QuestionDescriptor(questionId, normalizeLookupValue(question.path("prompt").asText(null)), tags));
        }
        return result;
    }

    private QuestionDescriptor resolveQuestion(JsonNode answer, List<QuestionDescriptor> questions, int answerIndex) {
        String questionId = answer.path("questionId").asText(null);
        if (questionId != null && !questionId.isBlank()) {
            for (QuestionDescriptor question : questions) {
                if (question.id().equals(questionId)) {
                    return question;
                }
            }
        }

        String prompt = normalizeLookupValue(answer.path("question").asText(null));
        if (prompt != null) {
            for (QuestionDescriptor question : questions) {
                if (prompt.equals(question.normalizedPrompt())) {
                    return question;
                }
            }
        }

        if (answerIndex >= 0 && answerIndex < questions.size()) {
            return questions.get(answerIndex);
        }

        return null;
    }

    private String normalizeLookupValue(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", " ")
            .trim()
            .replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }

    private Set<String> distinctTopicTags(List<QuestionDescriptor> questions) {
        Set<String> result = new LinkedHashSet<>();
        for (QuestionDescriptor question : questions) {
            result.addAll(question.tags());
        }
        return result;
    }

    private List<StudentAttempt> latestAttempts(List<StudentAttempt> attempts) {
        Map<String, StudentAttempt> byKey = new LinkedHashMap<>();
        for (StudentAttempt attempt : attempts) {
            String key = attempt.getStudent().getId() + "|" + attempt.getClassroom().getId() + "|" + attempt.getTopic().getId() + "|" + attempt.getWeekNumber();
            StudentAttempt current = byKey.get(key);
            if (current == null || compareByRecency(attempt, current) > 0) {
                byKey.put(key, attempt);
            }
        }
        return new ArrayList<>(byKey.values());
    }

    private int compareByRecency(StudentAttempt left, StudentAttempt right) {
        int byCompletedAt = left.getCompletedAt().compareTo(right.getCompletedAt());
        if (byCompletedAt != 0) {
            return byCompletedAt;
        }
        return Integer.compare(left.getScore(), right.getScore());
    }

    private boolean isTagInUse(UUID teacherId, String slug) {
        return learningTopicRepository.findAllByTeacherIdOrderByCreatedAtDesc(teacherId).stream()
            .map(LearningTopic::getQuestionsJson)
            .map(support::parseJson)
            .filter(JsonNode::isArray)
            .flatMap(questions -> {
                List<JsonNode> nodes = new ArrayList<>();
                questions.forEach(nodes::add);
                return nodes.stream();
            })
            .map(question -> question.get("pedagogicalTags"))
            .filter(node -> node != null && node.isArray())
            .flatMap(node -> {
                List<JsonNode> nodes = new ArrayList<>();
                node.forEach(nodes::add);
                return nodes.stream();
            })
            .map(JsonNode::asText)
            .anyMatch(slug::equals);
    }

    private PedagogicalTag ownedTag(UUID tagId) {
        return pedagogicalTagRepository.findByIdAndTeacherId(tagId, SecurityUtils.currentUserId())
            .orElseThrow(() -> new ResourceNotFoundException("PEDAGOGICAL_TAG_NOT_FOUND", "Pedagogical tag not found"));
    }

    private AppClassroom ownedClassroom(UUID classroomId) {
        return classroomRepository.findByIdAndTeacherId(classroomId, SecurityUtils.currentUserId())
            .orElseThrow(() -> new ResourceNotFoundException("CLASSROOM_NOT_FOUND", "Classroom not found"));
    }

    private User currentUser() {
        return userRepository.findById(SecurityUtils.currentUserId())
            .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));
    }

    private PedagogicalTagResponse response(PedagogicalTag tag) {
        return new PedagogicalTagResponse(tag.getId(), tag.getName(), tag.getSlug(), tag.getCreatedAt());
    }

    private String slugify(String raw) {
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("^_+|_+$", "");
        return normalized;
    }

    private static final class PedagogicalTagAccumulator {
        private long questionCount;
        private long answeredCount;
        private long correctCount;
        private long attemptCount;
        private long totalScore;
    }

    private record QuestionDescriptor(String id, String normalizedPrompt, Set<String> tags) {
    }
}
