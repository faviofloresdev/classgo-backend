package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.QuestionOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption, UUID> {
    List<QuestionOption> findByQuestionIdOrderBySortOrderAsc(UUID questionId);
    Optional<QuestionOption> findByIdAndQuestionId(UUID id, UUID questionId);
}
