package com.classgo.backend.application.achievements;

import com.classgo.backend.api.learning.dto.LearningDtos.AchievementUnlockedResponse;
import com.classgo.backend.domain.enums.AchievementAction;
import com.classgo.backend.domain.enums.AchievementActor;
import com.classgo.backend.domain.model.Achievement;
import com.classgo.backend.domain.model.User;
import com.classgo.backend.domain.model.UserAchievementMetric;
import com.classgo.backend.domain.repository.AchievementRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AchievementEvaluationService {

    private final AchievementRepository achievementRepository;
    private final AchievementUnlockService achievementUnlockService;

    public AchievementEvaluationService(
        AchievementRepository achievementRepository,
        AchievementUnlockService achievementUnlockService
    ) {
        this.achievementRepository = achievementRepository;
        this.achievementUnlockService = achievementUnlockService;
    }

    public List<AchievementUnlockedResponse> evaluate(
        User user,
        UserAchievementMetric metrics,
        AchievementAction action,
        AchievementActor actor,
        Instant happenedAt
    ) {
        List<Achievement> achievements = achievementRepository.findAllByActionAndActorAndActiveTrueOrderBySortOrderAsc(action, actor);
        return achievementUnlockService.applyProgress(user, achievements, achievement -> progressFor(achievement, metrics), happenedAt);
    }

    private long progressFor(Achievement achievement, UserAchievementMetric metrics) {
        return switch (achievement.getCode()) {
            case "PROGRESS_CHALLENGE_1", "PROGRESS_CHALLENGE_3", "PROGRESS_CHALLENGE_5", "PROGRESS_CHALLENGE_10",
                "PROGRESS_CHALLENGE_20", "PROGRESS_CHALLENGE_35", "PROGRESS_CHALLENGE_50", "PROGRESS_CHALLENGE_75",
                "PROGRESS_CHALLENGE_100" -> metrics.getCompletedChallengesCount();
            case "CONSISTENCY_WEEK_2", "CONSISTENCY_WEEK_3", "CONSISTENCY_WEEK_5", "CONSISTENCY_WEEK_8",
                "CONSISTENCY_WEEK_12", "CONSISTENCY_WEEK_20", "CONSISTENCY_WEEK_30" -> metrics.getCurrentWeeklyStreak();
            case "PERFORMANCE_HIGH_1", "PERFORMANCE_HIGH_3", "PERFORMANCE_HIGH_10" -> metrics.getHighScoreChallengesCount();
            case "PERFORMANCE_PERFECT_1", "PERFORMANCE_PERFECT_5", "PERFORMANCE_PERFECT_10" -> metrics.getPerfectScoreChallengesCount();
            case "EXPLORATION_SECTIONS_3" -> metrics.getDistinctSectionsCount();
            case "EXPLORATION_FEATURE_1", "EXPLORATION_FEATURE_3" -> metrics.getDistinctFeaturesCount();
            case "EXPLORATION_ACTIVITY_TYPE_1", "EXPLORATION_ACTIVITY_TYPE_3" -> metrics.getDistinctActivityTypesCount();
            case "SPECIAL_FIRST_COMPLETION" -> metrics.getFirstCompletionCount();
            default -> 1L;
        };
    }
}
