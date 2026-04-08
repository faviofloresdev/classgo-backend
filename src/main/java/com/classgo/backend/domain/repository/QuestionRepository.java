package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.Question;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findByTopicIdOrderBySortOrderAsc(UUID topicId);
    long countByTopicId(UUID topicId);
}
