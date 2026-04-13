package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.Achievement;
import com.classgo.backend.domain.model.UserAchievement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {
    Optional<UserAchievement> findByUserIdAndAchievement(UUID userId, Achievement achievement);
    List<UserAchievement> findAllByUserIdOrderByCreatedAtAsc(UUID userId);
}
