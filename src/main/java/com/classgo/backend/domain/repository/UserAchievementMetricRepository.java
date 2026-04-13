package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.UserAchievementMetric;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAchievementMetricRepository extends JpaRepository<UserAchievementMetric, UUID> {
    Optional<UserAchievementMetric> findByUserId(UUID userId);
}
