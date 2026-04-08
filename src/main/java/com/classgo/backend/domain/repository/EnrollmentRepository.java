package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.AppClassroom;
import com.classgo.backend.domain.model.Enrollment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    boolean existsByClassroomIdAndStudentId(UUID classroomId, UUID studentId);
    List<Enrollment> findAllByClassroomOrderByJoinedAtAsc(AppClassroom classroom);
    List<Enrollment> findAllByStudentIdOrderByJoinedAtDesc(UUID studentId);
    Optional<Enrollment> findByClassroomIdAndStudentId(UUID classroomId, UUID studentId);
    void deleteByClassroomIdAndStudentId(UUID classroomId, UUID studentId);
}
