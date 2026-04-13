package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.enums.AchievementFactType;
import com.classgo.backend.domain.model.AchievementGlobalFact;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AchievementGlobalFactRepository extends JpaRepository<AchievementGlobalFact, UUID> {
    Optional<AchievementGlobalFact> findByFactTypeAndFactKey(AchievementFactType factType, String factKey);
}
