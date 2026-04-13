package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.enums.AchievementFactType;
import com.classgo.backend.domain.model.UserAchievementFact;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAchievementFactRepository extends JpaRepository<UserAchievementFact, UUID> {
    boolean existsByUserIdAndFactTypeAndFactKey(UUID userId, AchievementFactType factType, String factKey);
    Optional<UserAchievementFact> findByUserIdAndFactTypeAndFactKey(UUID userId, AchievementFactType factType, String factKey);
}
