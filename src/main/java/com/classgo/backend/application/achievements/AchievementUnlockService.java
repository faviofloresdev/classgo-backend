package com.classgo.backend.application.achievements;

import com.classgo.backend.api.learning.dto.LearningDtos.AchievementUnlockedResponse;
import com.classgo.backend.domain.model.Achievement;
import com.classgo.backend.domain.model.User;
import com.classgo.backend.domain.model.UserAchievement;
import com.classgo.backend.domain.repository.UserAchievementRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AchievementUnlockService {

    private final UserAchievementRepository userAchievementRepository;

    public AchievementUnlockService(UserAchievementRepository userAchievementRepository) {
        this.userAchievementRepository = userAchievementRepository;
    }

    public List<AchievementUnlockedResponse> applyProgress(
        User user,
        List<Achievement> achievements,
        java.util.function.ToLongFunction<Achievement> progressResolver,
        Instant happenedAt
    ) {
        List<AchievementUnlockedResponse> unlocked = new ArrayList<>();
        for (Achievement achievement : achievements) {
            long progressValue = progressResolver.applyAsLong(achievement);
            UserAchievement state = userAchievementRepository.findByUserIdAndAchievement(user.getId(), achievement)
                .orElseGet(() -> {
                    UserAchievement created = new UserAchievement();
                    created.setUser(user);
                    created.setAchievement(achievement);
                    return created;
                });
            state.setProgressValue(progressValue);
            boolean shouldUnlock = !achievement.isManual() && progressValue >= requiredThreshold(achievement);
            if (shouldUnlock && !state.isUnlocked()) {
                state.setUnlocked(true);
                state.setUnlockedAt(happenedAt);
                unlocked.add(new AchievementUnlockedResponse(
                    achievement.getCode(),
                    achievement.getName(),
                    achievement.getCategory(),
                    happenedAt
                ));
            }
            userAchievementRepository.save(state);
        }
        return unlocked;
    }

    private long requiredThreshold(Achievement achievement) {
        return achievement.getThreshold() != null ? achievement.getThreshold() : 1L;
    }
}
