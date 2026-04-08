package com.classgo.backend.application.gameplay;

import com.classgo.backend.api.gameplay.dto.GameplayDtos.EndSessionRequest;
import com.classgo.backend.api.gameplay.dto.GameplayDtos.SessionResponse;
import com.classgo.backend.api.gameplay.dto.GameplayDtos.SessionSummaryResponse;
import com.classgo.backend.api.gameplay.dto.GameplayDtos.StartSessionRequest;
import com.classgo.backend.api.gameplay.dto.GameplayDtos.SubmitAnswerRequest;
import com.classgo.backend.api.gameplay.dto.GameplayDtos.SubmitAnswerResponse;
import com.classgo.backend.api.results.dto.ResultDtos.ProgressResponse;
import com.classgo.backend.application.results.ResultService;
import com.classgo.backend.application.students.StudentAffiliationService;
import com.classgo.backend.domain.enums.ActivityEventType;
import com.classgo.backend.domain.enums.QuestionType;
import com.classgo.backend.domain.enums.SessionStatus;
import com.classgo.backend.domain.enums.UserRole;
import com.classgo.backend.domain.model.Question;
import com.classgo.backend.domain.model.QuestionOption;
import com.classgo.backend.domain.model.Student;
import com.classgo.backend.domain.model.StudentActivityEvent;
import com.classgo.backend.domain.model.StudentAnswer;
import com.classgo.backend.domain.model.StudentSession;
import com.classgo.backend.domain.model.WeeklyChallenge;
import com.classgo.backend.domain.repository.ChallengeParticipantRepository;
import com.classgo.backend.domain.repository.QuestionOptionRepository;
import com.classgo.backend.domain.repository.QuestionRepository;
import com.classgo.backend.domain.repository.StudentActivityEventRepository;
import com.classgo.backend.domain.repository.StudentAnswerRepository;
import com.classgo.backend.domain.repository.StudentRepository;
import com.classgo.backend.domain.repository.StudentSessionRepository;
import com.classgo.backend.domain.repository.WeeklyChallengeRepository;
import com.classgo.backend.infrastructure.security.SecurityUtils;
import com.classgo.backend.shared.exception.BusinessRuleViolationException;
import com.classgo.backend.shared.exception.ResourceNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameplayService {

    private final StudentSessionRepository sessionRepository;
    private final StudentActivityEventRepository eventRepository;
    private final StudentAnswerRepository answerRepository;
    private final WeeklyChallengeRepository challengeRepository;
    private final ChallengeParticipantRepository participantRepository;
    private final StudentRepository studentRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final StudentAffiliationService affiliationService;
    private final ResultService resultService;

    public GameplayService(
        StudentSessionRepository sessionRepository,
        StudentActivityEventRepository eventRepository,
        StudentAnswerRepository answerRepository,
        WeeklyChallengeRepository challengeRepository,
        ChallengeParticipantRepository participantRepository,
        StudentRepository studentRepository,
        QuestionRepository questionRepository,
        QuestionOptionRepository questionOptionRepository,
        StudentAffiliationService affiliationService,
        ResultService resultService
    ) {
        this.sessionRepository = sessionRepository;
        this.eventRepository = eventRepository;
        this.answerRepository = answerRepository;
        this.challengeRepository = challengeRepository;
        this.participantRepository = participantRepository;
        this.studentRepository = studentRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.affiliationService = affiliationService;
        this.resultService = resultService;
    }

    @Transactional
    public SessionResponse start(StartSessionRequest request) {
        SecurityUtils.requireRole(UserRole.PARENT);
        affiliationService.getParentLinkOrThrow(SecurityUtils.currentUserId(), request.studentId());
        Student student = studentRepository.findById(request.studentId()).orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        WeeklyChallenge challenge = challengeRepository.findById(request.challengeId())
            .orElseThrow(() -> new ResourceNotFoundException("Challenge not found"));
        if (!participantRepository.existsByChallengeIdAndStudentId(challenge.getId(), student.getId())) {
            throw new BusinessRuleViolationException("Student is not a challenge participant");
        }
        StudentSession session = sessionRepository.findFirstByStudentIdAndChallengeIdAndStatus(student.getId(), challenge.getId(), SessionStatus.ACTIVE)
            .orElseGet(() -> {
                StudentSession created = new StudentSession();
                created.setStudent(student);
                created.setChallenge(challenge);
                created.setStartedAt(Instant.now());
                created.setLastActivityAt(Instant.now());
                return sessionRepository.save(created);
            });
        recordEvent(session, ActivityEventType.SESSION_STARTED, "{}", Instant.now());
        return new SessionResponse(session.getId(), session.getStatus().name(), session.getStartedAt());
    }

    @Transactional
    public void heartbeat(UUID sessionId, Instant timestamp) {
        StudentSession session = activeSession(sessionId);
        session.setLastActivityAt(timestamp);
        sessionRepository.save(session);
        recordEvent(session, ActivityEventType.HEARTBEAT, "{}", timestamp);
    }

    @Transactional
    public SubmitAnswerResponse answer(SubmitAnswerRequest request) {
        StudentSession session = activeSession(request.sessionId());
        if (!session.getStudent().getId().equals(request.studentId()) || !session.getChallenge().getId().equals(request.challengeId())) {
            throw new BusinessRuleViolationException("Session does not match request payload");
        }
        Question question = questionRepository.findById(request.questionId()).orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        if (!question.getTopic().getId().equals(session.getChallenge().getTopic().getId())) {
            throw new BusinessRuleViolationException("Question does not belong to the challenge topic");
        }
        if (answerRepository.existsByStudentIdAndChallengeIdAndQuestionId(request.studentId(), request.challengeId(), request.questionId())) {
            throw new BusinessRuleViolationException("Question already answered");
        }
        QuestionOption selectedOption = null;
        boolean correct = false;
        UUID correctOptionId = null;
        if (question.getType() == QuestionType.MULTIPLE_CHOICE) {
            selectedOption = questionOptionRepository.findByIdAndQuestionId(request.selectedOptionId(), question.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Option not found"));
            correct = selectedOption.isCorrect();
            correctOptionId = questionOptionRepository.findByQuestionIdOrderBySortOrderAsc(question.getId()).stream()
                .filter(QuestionOption::isCorrect)
                .findFirst()
                .map(QuestionOption::getId)
                .orElse(null);
        }
        StudentAnswer answer = new StudentAnswer();
        answer.setStudent(session.getStudent());
        answer.setChallenge(session.getChallenge());
        answer.setQuestion(question);
        answer.setSelectedOption(selectedOption);
        answer.setAnswerText(request.answerText());
        answer.setCorrect(correct);
        answer.setPointsEarned(correct ? 10 : 0);
        answer.setAnsweredAt(Instant.now());
        answerRepository.save(answer);
        recordEvent(session, ActivityEventType.ANSWER_SUBMITTED, "{\"questionId\":\"" + question.getId() + "\"}", Instant.now());
        recordEvent(session, correct ? ActivityEventType.ANSWER_CORRECT : ActivityEventType.ANSWER_INCORRECT, "{}", Instant.now());
        ProgressResponse progress = resultService.progress(request.studentId(), request.challengeId());
        int streak = computeStreak(answerRepository.findByStudentIdAndChallengeId(request.studentId(), request.challengeId()));
        return new SubmitAnswerResponse(correct, answer.getPointsEarned(), correctOptionId, question.getExplanation(), streak, progress);
    }

    @Transactional
    public SessionSummaryResponse end(UUID sessionId, EndSessionRequest request) {
        StudentSession session = activeSession(sessionId);
        session.setStatus(SessionStatus.ENDED);
        session.setEndedAt(request.timestamp());
        session.setTotalSeconds(Duration.between(session.getStartedAt(), request.timestamp()).toSeconds());
        session.setActiveSeconds(calculateActiveSeconds(session));
        sessionRepository.save(session);
        recordEvent(session, ActivityEventType.SESSION_ENDED, "{}", request.timestamp());
        long answered = answerRepository.countByStudentIdAndChallengeId(session.getStudent().getId(), session.getChallenge().getId());
        long correct = answerRepository.countByStudentIdAndChallengeIdAndCorrectTrue(session.getStudent().getId(), session.getChallenge().getId());
        int points = answerRepository.findByStudentIdAndChallengeId(session.getStudent().getId(), session.getChallenge().getId()).stream()
            .mapToInt(StudentAnswer::getPointsEarned)
            .sum();
        return new SessionSummaryResponse(session.getId(), session.getStatus().name(), session.getTotalSeconds(), session.getActiveSeconds(),
            new SessionSummaryResponse.Summary(answered, correct, points));
    }

    private StudentSession activeSession(UUID sessionId) {
        StudentSession session = sessionRepository.findById(sessionId).orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new BusinessRuleViolationException("Session is not active");
        }
        return session;
    }

    private void recordEvent(StudentSession session, ActivityEventType type, String payload, Instant happenedAt) {
        StudentActivityEvent event = new StudentActivityEvent();
        event.setSession(session);
        event.setEventType(type);
        event.setEventPayload(payload);
        event.setHappenedAt(happenedAt);
        eventRepository.save(event);
    }

    private long calculateActiveSeconds(StudentSession session) {
        List<StudentActivityEvent> events = eventRepository.findBySessionIdOrderByHappenedAtAsc(session.getId());
        long total = 0L;
        for (int i = 1; i < events.size(); i++) {
            long diff = Duration.between(events.get(i - 1).getHappenedAt(), events.get(i).getHappenedAt()).toSeconds();
            total += Math.max(0L, Math.min(diff, 60L));
        }
        return total;
    }

    private int computeStreak(List<StudentAnswer> answers) {
        int streak = 0;
        for (int i = answers.size() - 1; i >= 0; i--) {
            if (answers.get(i).isCorrect()) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }
}
