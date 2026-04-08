package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.enums.ChallengeStatus;
import com.classgo.backend.domain.model.WeeklyChallenge;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyChallengeRepository extends JpaRepository<WeeklyChallenge, UUID> {
    Optional<WeeklyChallenge> findByIdAndCreatedByTeacherUserId(UUID id, UUID teacherUserId);
    List<WeeklyChallenge> findByStatusAndEndDateBefore(ChallengeStatus status, LocalDate date);
    Optional<WeeklyChallenge> findFirstByStatusAndClassroomIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        ChallengeStatus status,
        UUID classroomId,
        LocalDate startDate,
        LocalDate endDate
    );
    Optional<WeeklyChallenge> findFirstByStatusAndClassroomId(ChallengeStatus status, UUID classroomId);
}
