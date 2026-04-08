package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.AppClassroom;
import com.classgo.backend.domain.model.StudyPlan;
import com.classgo.backend.domain.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppClassroomRepository extends JpaRepository<AppClassroom, UUID> {
    List<AppClassroom> findAllByTeacherOrderByCreatedAtDesc(User teacher);
    List<AppClassroom> findAllByActivePlanOrderByCreatedAtDesc(StudyPlan plan);
    Optional<AppClassroom> findByIdAndTeacherId(UUID id, UUID teacherId);
    Optional<AppClassroom> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
    long countByActivePlanId(UUID activePlanId);
}
