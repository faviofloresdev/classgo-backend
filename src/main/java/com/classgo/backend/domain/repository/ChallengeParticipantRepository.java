package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.ChallengeParticipant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeParticipantRepository extends JpaRepository<ChallengeParticipant, UUID> {
    boolean existsByChallengeIdAndStudentId(UUID challengeId, UUID studentId);
    List<ChallengeParticipant> findByChallengeId(UUID challengeId);
}
