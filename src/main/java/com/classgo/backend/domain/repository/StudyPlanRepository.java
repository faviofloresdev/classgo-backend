package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.StudyPlan;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, UUID> {
    List<StudyPlan> findAllByTeacherIdOrderByCreatedAtDesc(UUID teacherId);
    Optional<StudyPlan> findByIdAndTeacherId(UUID id, UUID teacherId);
    List<StudyPlan> findAllByActivationMode(com.classgo.backend.domain.enums.ActivationMode activationMode);
}
