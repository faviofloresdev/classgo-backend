package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.Topic;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, UUID> {
    List<Topic> findBySubjectIdAndGrade(UUID subjectId, String grade);
}
