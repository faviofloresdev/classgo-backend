package com.classgo.backend.application.achievements;

import com.classgo.backend.api.learning.dto.LearningDtos.AchievementProgressResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.AchievementUpdateResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.AchievementUnlockedResponse;
import com.classgo.backend.domain.enums.AchievementAction;
import com.classgo.backend.domain.enums.AchievementActivityType;
import com.classgo.backend.domain.enums.AchievementActor;
import com.classgo.backend.domain.enums.AchievementFactType;
import com.classgo.backend.domain.enums.AchievementFeature;
import com.classgo.backend.domain.enums.AchievementSection;
import com.classgo.backend.domain.enums.UserRole;
import com.classgo.backend.domain.model.AchievementGlobalFact;
import com.classgo.backend.domain.model.StudentAttempt;
import com.classgo.backend.domain.model.User;
import com.classgo.backend.domain.model.UserAchievementFact;
import com.classgo.backend.domain.model.UserAchievementMetric;
import com.classgo.backend.domain.repository.AchievementGlobalFactRepository;
import com.classgo.backend.domain.repository.UserAchievementFactRepository;
import com.classgo.backend.domain.repository.UserAchievementMetricRepository;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AchievementEventService {

    private final UserAchievementMetricRepository metricRepository;
    private final UserAchievementFactRepository factRepository;
    private final AchievementGlobalFactRepository globalFactRepository;
    private final AchievementEvaluationService evaluationService;
    private final ZoneId achievementZone = ZoneId.systemDefault();

    public AchievementEventService(
        UserAchievementMetricRepository metricRepository,
        UserAchievementFactRepository factRepository,
        AchievementGlobalFactRepository globalFactRepository,
        AchievementEvaluationService evaluationService
    ) {
        this.metricRepository = metricRepository;
        this.factRepository = factRepository;
        this.globalFactRepository = globalFactRepository;
        this.evaluationService = evaluationService;
    }

    @Transactional
    public AchievementUpdateResponse onChallengeCompleted(User user, StudentAttempt attempt) {
        UserAchievementMetric metrics = metricFor(user);
        List<AchievementUnlockedResponse> unlocked = new ArrayList<>();
        Instant happenedAt = attempt.getCompletedAt();
        String challengeKey = challengeKey(attempt);

        if (recordFact(user, AchievementFactType.COMPLETED_CHALLENGE, challengeKey)) {
            metrics.setCompletedChallengesCount(metrics.getCompletedChallengesCount() + 1);
            LocalDate weekStart = weekStart(attempt.getCompletedAt());
            String weekKey = weekStart.toString();
            if (recordFact(user, AchievementFactType.COMPLETED_WEEK, weekKey)) {
                boolean previousWeekExists = factRepository.existsByUserIdAndFactTypeAndFactKey(
                    user.getId(),
                    AchievementFactType.COMPLETED_WEEK,
                    weekStart.minusWeeks(1).toString()
                );
                metrics.setCurrentWeeklyStreak(previousWeekExists ? metrics.getCurrentWeeklyStreak() + 1 : 1);
            }
            unlocked.addAll(evaluationService.evaluate(user, metrics, AchievementAction.COMPLETE_CHALLENGE, AchievementActor.STUDENT, happenedAt));
        }

        if (attempt.getScore() >= 90 && recordFact(user, AchievementFactType.CHALLENGE_HIGH_SCORE, challengeKey)) {
            metrics.setHighScoreChallengesCount(metrics.getHighScoreChallengesCount() + 1);
            unlocked.addAll(evaluationService.evaluate(user, metrics, AchievementAction.RECORD_CHALLENGE_SCORE, AchievementActor.STUDENT, happenedAt));
        }
        if (attempt.getScore() == 100 && recordFact(user, AchievementFactType.CHALLENGE_PERFECT, challengeKey)) {
            metrics.setPerfectScoreChallengesCount(metrics.getPerfectScoreChallengesCount() + 1);
            unlocked.addAll(evaluationService.evaluate(user, metrics, AchievementAction.RECORD_CHALLENGE_SCORE, AchievementActor.STUDENT, happenedAt));
        }
        if (claimGlobalFact(AchievementFactType.FIRST_COMPLETION_AWARDED, challengeKey, user)) {
            metrics.setFirstCompletionCount(metrics.getFirstCompletionCount() + 1);
            unlocked.addAll(evaluationService.evaluate(user, metrics, AchievementAction.FIRST_TO_COMPLETE_CHALLENGE, AchievementActor.STUDENT, happenedAt));
        }

        metricRepository.save(metrics);
        return new AchievementUpdateResponse(deduplicate(unlocked), progress(metrics));
    }

    @Transactional
    public AchievementUpdateResponse onProfileUpdated(User user, String previousName, String previousAvatarId, String previousParentAvatarId) {
        UserAchievementMetric metrics = metricFor(user);
        List<AchievementUnlockedResponse> unlocked = new ArrayList<>();
        Instant happenedAt = Instant.now();
        boolean profileChanged = changed(previousName, user.getName()) || changed(previousAvatarId, user.getAvatarId()) || changed(previousParentAvatarId, user.getParentAvatarId());

        if (profileChanged) {
            unlocked.addAll(trackFeatureUseInternal(user, metrics, AchievementFeature.EDIT_PROFILE, happenedAt));
        }
        if (user.getRole() == UserRole.STUDENT && changed(previousAvatarId, user.getAvatarId())
            && recordFact(user, AchievementFactType.AVATAR_UPDATED_STUDENT, "student-avatar")) {
            unlocked.addAll(trackFeatureUseInternal(user, metrics, AchievementFeature.UPDATE_AVATAR, happenedAt));
            unlocked.addAll(evaluationService.evaluate(user, metrics, AchievementAction.UPDATE_AVATAR, AchievementActor.STUDENT, happenedAt));
        }
        if (changed(previousParentAvatarId, user.getParentAvatarId())
            && recordFact(user, AchievementFactType.AVATAR_UPDATED_PARENT, "parent-avatar")) {
            unlocked.addAll(trackFeatureUseInternal(user, metrics, AchievementFeature.UPDATE_AVATAR, happenedAt));
            unlocked.addAll(evaluationService.evaluate(user, metrics, AchievementAction.UPDATE_AVATAR, AchievementActor.PARENT, happenedAt));
        }
        if (user.getRole() == UserRole.STUDENT && isStudentProfileComplete(user)
            && recordFact(user, AchievementFactType.PROFILE_COMPLETED, "student-profile")) {
            unlocked.addAll(evaluationService.evaluate(user, metrics, AchievementAction.COMPLETE_PROFILE, AchievementActor.STUDENT, happenedAt));
        }

        metricRepository.save(metrics);
        return new AchievementUpdateResponse(deduplicate(unlocked), progress(metrics));
    }

    @Transactional
    public AchievementUpdateResponse onSectionVisited(User user, AchievementSection section) {
        UserAchievementMetric metrics = metricFor(user);
        List<AchievementUnlockedResponse> unlocked = new ArrayList<>();
        Instant happenedAt = Instant.now();
        if (recordFact(user, AchievementFactType.SECTION_VISITED, section.name())) {
            metrics.setDistinctSectionsCount(metrics.getDistinctSectionsCount() + 1);
            unlocked.addAll(evaluationService.evaluate(user, metrics, AchievementAction.VISIT_SECTION, AchievementActor.STUDENT, happenedAt));
            metricRepository.save(metrics);
        }
        return new AchievementUpdateResponse(deduplicate(unlocked), progress(metrics));
    }

    @Transactional
    public AchievementUpdateResponse onFeatureUsed(User user, AchievementFeature feature) {
        UserAchievementMetric metrics = metricFor(user);
        List<AchievementUnlockedResponse> unlocked = trackFeatureUseInternal(user, metrics, feature, Instant.now());
        metricRepository.save(metrics);
        return new AchievementUpdateResponse(deduplicate(unlocked), progress(metrics));
    }

    @Transactional
    public AchievementUpdateResponse onActivityTypeCompleted(User user, AchievementActivityType activityType) {
        UserAchievementMetric metrics = metricFor(user);
        List<AchievementUnlockedResponse> unlocked = new ArrayList<>();
        Instant happenedAt = Instant.now();
        if (recordFact(user, AchievementFactType.ACTIVITY_TYPE_COMPLETED, activityType.name())) {
            metrics.setDistinctActivityTypesCount(metrics.getDistinctActivityTypesCount() + 1);
            unlocked.addAll(evaluationService.evaluate(user, metrics, AchievementAction.COMPLETE_ACTIVITY_TYPE, AchievementActor.STUDENT, happenedAt));
            metricRepository.save(metrics);
        }
        return new AchievementUpdateResponse(deduplicate(unlocked), progress(metrics));
    }

    private List<AchievementUnlockedResponse> trackFeatureUseInternal(
        User user,
        UserAchievementMetric metrics,
        AchievementFeature feature,
        Instant happenedAt
    ) {
        List<AchievementUnlockedResponse> unlocked = new ArrayList<>();
        if (recordFact(user, AchievementFactType.FEATURE_USED, feature.name())) {
            metrics.setDistinctFeaturesCount(metrics.getDistinctFeaturesCount() + 1);
            unlocked.addAll(evaluationService.evaluate(user, metrics, AchievementAction.USE_FEATURE, AchievementActor.STUDENT, happenedAt));
        }
        return unlocked;
    }

    private UserAchievementMetric metricFor(User user) {
        return metricRepository.findByUserId(user.getId()).orElseGet(() -> {
            UserAchievementMetric created = new UserAchievementMetric();
            created.setUser(user);
            return metricRepository.save(created);
        });
    }

    private boolean recordFact(User user, AchievementFactType type, String key) {
        if (factRepository.existsByUserIdAndFactTypeAndFactKey(user.getId(), type, key)) {
            return false;
        }
        UserAchievementFact fact = new UserAchievementFact();
        fact.setUser(user);
        fact.setFactType(type);
        fact.setFactKey(key);
        try {
            factRepository.save(fact);
            return true;
        } catch (DataIntegrityViolationException ex) {
            return false;
        }
    }

    private boolean claimGlobalFact(AchievementFactType type, String key, User user) {
        if (globalFactRepository.findByFactTypeAndFactKey(type, key).isPresent()) {
            return false;
        }
        AchievementGlobalFact fact = new AchievementGlobalFact();
        fact.setFactType(type);
        fact.setFactKey(key);
        fact.setUser(user);
        try {
            globalFactRepository.save(fact);
            return true;
        } catch (DataIntegrityViolationException ex) {
            return false;
        }
    }

    private AchievementProgressResponse progress(UserAchievementMetric metrics) {
        return new AchievementProgressResponse(
            metrics.getCompletedChallengesCount(),
            metrics.getCurrentWeeklyStreak(),
            metrics.getHighScoreChallengesCount(),
            metrics.getPerfectScoreChallengesCount(),
            metrics.getDistinctSectionsCount(),
            metrics.getDistinctFeaturesCount(),
            metrics.getDistinctActivityTypesCount(),
            metrics.getFirstCompletionCount()
        );
    }

    private List<AchievementUnlockedResponse> deduplicate(List<AchievementUnlockedResponse> unlocked) {
        java.util.LinkedHashMap<String, AchievementUnlockedResponse> unique = new java.util.LinkedHashMap<>();
        for (AchievementUnlockedResponse item : unlocked) {
            unique.put(item.code(), item);
        }
        return new ArrayList<>(unique.values());
    }

    private boolean isStudentProfileComplete(User user) {
        return user.getRole() == UserRole.STUDENT
            && user.getName() != null && !user.getName().isBlank()
            && user.getAvatarId() != null && !user.getAvatarId().isBlank();
    }

    private boolean changed(String before, String after) {
        String left = before == null ? "" : before.trim();
        String right = after == null ? "" : after.trim();
        return !left.equals(right);
    }

    private LocalDate weekStart(Instant instant) {
        return instant.atZone(achievementZone).toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private String challengeKey(StudentAttempt attempt) {
        return attempt.getClassroom().getId() + ":" + attempt.getTopic().getId() + ":" + attempt.getWeekNumber();
    }
}
