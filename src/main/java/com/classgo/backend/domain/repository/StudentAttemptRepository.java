package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.StudentAttempt;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentAttemptRepository extends JpaRepository<StudentAttempt, UUID> {
    List<StudentAttempt> findAllByClassroomIdOrderByWeekNumberAscCompletedAtAsc(UUID classroomId);
    List<StudentAttempt> findAllByClassroomIdAndWeekNumberOrderByCompletedAtAsc(UUID classroomId, int weekNumber);
    List<StudentAttempt> findAllByStudentIdOrderByCompletedAtDesc(UUID studentId);
    List<StudentAttempt> findAllByStudentIdAndClassroomIdOrderByCompletedAtDesc(UUID studentId, UUID classroomId);
    List<StudentAttempt> findAllByStudentIdAndClassroomIdAndTopicIdAndWeekNumberOrderByCompletedAtDesc(
        UUID studentId,
        UUID classroomId,
        UUID topicId,
        int weekNumber
    );
    List<StudentAttempt> findAllByClassroomIdAndStudentIdOrderByWeekNumberAscCompletedAtAsc(UUID classroomId, UUID studentId);
}
