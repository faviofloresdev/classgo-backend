package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.enums.AchievementAction;
import com.classgo.backend.domain.enums.AchievementActor;
import com.classgo.backend.domain.model.Achievement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AchievementRepository extends JpaRepository<Achievement, UUID> {
    List<Achievement> findAllByActionAndActorAndActiveTrueOrderBySortOrderAsc(AchievementAction action, AchievementActor actor);
    List<Achievement> findAllByActiveTrueOrderBySortOrderAsc();
    Optional<Achievement> findByCode(String code);
}
