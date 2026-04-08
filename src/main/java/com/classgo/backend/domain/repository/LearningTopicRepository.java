package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.LearningTopic;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningTopicRepository extends JpaRepository<LearningTopic, UUID> {
    List<LearningTopic> findAllByTeacherIdOrderByCreatedAtDesc(UUID teacherId);
    Optional<LearningTopic> findByIdAndTeacherId(UUID id, UUID teacherId);
}
