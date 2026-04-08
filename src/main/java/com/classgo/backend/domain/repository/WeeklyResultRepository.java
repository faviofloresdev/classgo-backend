package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.WeeklyResult;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyResultRepository extends JpaRepository<WeeklyResult, UUID> {
    boolean existsByChallengeId(UUID challengeId);
    Optional<WeeklyResult> findByChallengeIdAndStudentId(UUID challengeId, UUID studentId);
    List<WeeklyResult> findByChallengeIdOrderByRankPositionAsc(UUID challengeId);
}
