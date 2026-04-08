package com.classgo.backend.application.results;

import com.classgo.backend.api.results.dto.ResultDtos.LeaderboardEntry;
import com.classgo.backend.api.results.dto.ResultDtos.LeaderboardResponse;
import com.classgo.backend.api.results.dto.ResultDtos.ProgressResponse;
import com.classgo.backend.api.results.dto.ResultDtos.WeeklyResultResponse;
import com.classgo.backend.application.notifications.NotificationService;
import com.classgo.backend.domain.enums.ChallengeStatus;
import com.classgo.backend.domain.enums.NotificationType;
import com.classgo.backend.domain.model.ParentStudentLink;
import com.classgo.backend.domain.model.Student;
import com.classgo.backend.domain.model.StudentAnswer;
import com.classgo.backend.domain.model.StudentSession;
import com.classgo.backend.domain.model.WeeklyChallenge;
import com.classgo.backend.domain.model.WeeklyResult;
import com.classgo.backend.domain.repository.ChallengeParticipantRepository;
import com.classgo.backend.domain.repository.ParentStudentLinkRepository;
import com.classgo.backend.domain.repository.QuestionRepository;
import com.classgo.backend.domain.repository.StudentAnswerRepository;
import com.classgo.backend.domain.repository.StudentSessionRepository;
import com.classgo.backend.domain.repository.WeeklyChallengeRepository;
import com.classgo.backend.domain.repository.WeeklyResultRepository;
import com.classgo.backend.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResultService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ResultService.class);
    private final StudentAnswerRepository answerRepository;
    private final StudentSessionRepository sessionRepository;
    private final QuestionRepository questionRepository;
    private final WeeklyChallengeRepository challengeRepository;
    private final WeeklyResultRepository weeklyResultRepository;
    private final ChallengeParticipantRepository participantRepository;
    private final ParentStudentLinkRepository linkRepository;
    private final NotificationService notificationService;

    public ResultService(StudentAnswerRepository answerRepository, StudentSessionRepository sessionRepository, QuestionRepository questionRepository, WeeklyChallengeRepository challengeRepository, WeeklyResultRepository weeklyResultRepository, ChallengeParticipantRepository participantRepository, ParentStudentLinkRepository linkRepository, NotificationService notificationService) {
        this.answerRepository = answerRepository;
        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
        this.challengeRepository = challengeRepository;
        this.weeklyResultRepository = weeklyResultRepository;
        this.participantRepository = participantRepository;
        this.linkRepository = linkRepository;
        this.notificationService = notificationService;
    }

    public ProgressResponse progress(UUID studentId, UUID challengeId) {
        long answered = answerRepository.countByStudentIdAndChallengeId(studentId, challengeId);
        long correct = answerRepository.countByStudentIdAndChallengeIdAndCorrectTrue(studentId, challengeId);
        int points = answerRepository.findByStudentIdAndChallengeId(studentId, challengeId).stream().mapToInt(StudentAnswer::getPointsEarned).sum();
        long activeSeconds = sessionRepository.findByStudentIdAndChallengeId(studentId, challengeId).stream().mapToLong(StudentSession::getActiveSeconds).sum();
        long totalQuestions = questionRepository.countByTopicId(challengeRepository.findById(challengeId).orElseThrow(() -> new ResourceNotFoundException("Challenge not found")).getTopic().getId());
        BigDecimal accuracy = answered == 0 ? BigDecimal.ZERO : BigDecimal.valueOf((double) correct / answered).setScale(2, RoundingMode.HALF_UP);
        return new ProgressResponse(points, accuracy, answered, totalQuestions, answered, activeSeconds);
    }

    public LeaderboardResponse leaderboard(UUID challengeId, UUID currentStudentId) {
        List<WeeklyResult> storedResults = weeklyResultRepository.findByChallengeIdOrderByRankPositionAsc(challengeId);
        List<LeaderboardEntry> entries = mapEntries(storedResults);
        Integer currentRank = storedResults.stream().filter(result -> result.getStudent().getId().equals(currentStudentId)).map(WeeklyResult::getRankPosition).findFirst().orElse(null);
        if (!storedResults.isEmpty()) {
            return new LeaderboardResponse(challengeId, entries, currentRank);
        }
        WeeklyChallenge challenge = challengeRepository.findById(challengeId).orElseThrow(() -> new ResourceNotFoundException("Challenge not found"));
        List<Student> participants = participantRepository.findByChallengeId(challengeId).stream().map(participant -> participant.getStudent()).toList();
        List<WeeklyResult> partial = buildRankedResults(challenge, participants, false);
        entries = mapEntries(partial);
        currentRank = partial.stream().filter(result -> result.getStudent().getId().equals(currentStudentId)).map(WeeklyResult::getRankPosition).findFirst().orElse(null);
        return new LeaderboardResponse(challengeId, entries, currentRank);
    }

    public WeeklyResultResponse weeklyResult(UUID challengeId, UUID studentId) {
        return weeklyResultRepository.findByChallengeIdAndStudentId(challengeId, studentId).map(this::toResponse).orElseThrow(() -> new ResourceNotFoundException("Weekly result not found"));
    }

    @Transactional
    public void closeExpiredChallenges() {
        List<WeeklyChallenge> challenges = challengeRepository.findByStatusAndEndDateBefore(ChallengeStatus.PUBLISHED, LocalDate.now());
        for (WeeklyChallenge challenge : challenges) {
            if (weeklyResultRepository.existsByChallengeId(challenge.getId())) {
                challenge.setStatus(ChallengeStatus.CLOSED);
                challenge.setClosedAt(Instant.now());
                challengeRepository.save(challenge);
                continue;
            }
            List<Student> participants = participantRepository.findByChallengeId(challenge.getId()).stream().map(participant -> participant.getStudent()).toList();
            List<WeeklyResult> results = buildRankedResults(challenge, participants, true);
            weeklyResultRepository.saveAll(results);
            challenge.setStatus(ChallengeStatus.CLOSED);
            challenge.setClosedAt(Instant.now());
            challengeRepository.save(challenge);
            for (WeeklyResult result : results) {
                linkRepository.findAll().stream().filter(link -> link.getStudent().getId().equals(result.getStudent().getId())).forEach(link -> notificationService.send(link.getParent().getUser(), link.getParent().getUser().getEmail(), NotificationType.WEEKLY_RESULTS, "Weekly results available", "<p>" + link.getDisplayName() + " scored " + result.getTotalPoints() + " points</p>", "{\"challengeId\":\"" + challenge.getId() + "\",\"studentId\":\"" + result.getStudent().getId() + "\"}"));
            }
            log.info("Challenge closed {}", challenge.getId());
        }
    }

    private List<LeaderboardEntry> mapEntries(List<WeeklyResult> results) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (WeeklyResult result : results) {
            ParentStudentLink link = linkRepository.findAll().stream().filter(candidate -> candidate.getStudent().getId().equals(result.getStudent().getId())).findFirst().orElse(null);
            entries.add(new LeaderboardEntry(result.getRankPosition(), link != null ? link.getDisplayName() : "Student", link != null ? link.getAvatarId() : null, result.getTotalPoints()));
        }
        return entries;
    }

    private List<WeeklyResult> buildRankedResults(WeeklyChallenge challenge, List<Student> participants, boolean includeSummaries) {
        List<WeeklyResult> computed = participants.stream().map(student -> {
            ProgressResponse progress = progress(student.getId(), challenge.getId());
            WeeklyResult result = new WeeklyResult();
            result.setStudent(student);
            result.setChallenge(challenge);
            result.setTotalPoints(progress.points());
            result.setAccuracy(progress.accuracy());
            result.setCompletedActivities((int) progress.completedActivities());
            result.setActiveSeconds(progress.activeSeconds());
            result.setGeneratedAt(Instant.now());
            if (includeSummaries) {
                result.setStrengthsSummary(progress.accuracy().compareTo(BigDecimal.valueOf(0.7)) >= 0 ? "Strong progress in this topic" : "Shows effort and growing confidence");
                result.setWeaknessesSummary(progress.accuracy().compareTo(BigDecimal.valueOf(0.7)) >= 0 ? "Keep practicing to stay sharp" : "Needs reinforcement on key concepts");
            }
            return result;
        }).sorted(Comparator.comparingInt(WeeklyResult::getTotalPoints).reversed().thenComparing(WeeklyResult::getAccuracy, Comparator.reverseOrder()).thenComparing(Comparator.comparingLong(WeeklyResult::getActiveSeconds).reversed())).toList();
        List<WeeklyResult> ranked = new ArrayList<>();
        int rank = 1;
        for (WeeklyResult result : computed) {
            result.setRankPosition(rank++);
            ranked.add(result);
        }
        return ranked;
    }

    private WeeklyResultResponse toResponse(WeeklyResult result) {
        return new WeeklyResultResponse(result.getStudent().getId(), result.getChallenge().getId(), result.getTotalPoints(), result.getAccuracy(), result.getRankPosition(), result.getCompletedActivities(), result.getActiveSeconds(), result.getStrengthsSummary(), result.getWeaknessesSummary());
    }
}
