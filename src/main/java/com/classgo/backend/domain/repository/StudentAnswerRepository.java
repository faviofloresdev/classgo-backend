package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.StudentAnswer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, UUID> {
    boolean existsByStudentIdAndChallengeIdAndQuestionId(UUID studentId, UUID challengeId, UUID questionId);
    List<StudentAnswer> findByStudentIdAndChallengeId(UUID studentId, UUID challengeId);
    long countByStudentIdAndChallengeId(UUID studentId, UUID challengeId);
    long countByStudentIdAndChallengeIdAndCorrectTrue(UUID studentId, UUID challengeId);
    Optional<StudentAnswer> findFirstByStudentIdAndChallengeIdOrderByAnsweredAtDesc(UUID studentId, UUID challengeId);
}
