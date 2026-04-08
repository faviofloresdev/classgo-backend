package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.enums.SessionStatus;
import com.classgo.backend.domain.model.StudentSession;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentSessionRepository extends JpaRepository<StudentSession, UUID> {
    Optional<StudentSession> findFirstByStudentIdAndChallengeIdAndStatus(UUID studentId, UUID challengeId, SessionStatus status);
    List<StudentSession> findByStudentIdAndChallengeId(UUID studentId, UUID challengeId);
}
