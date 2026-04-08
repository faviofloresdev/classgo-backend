package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.PlanTopic;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanTopicRepository extends JpaRepository<PlanTopic, UUID> {
    List<PlanTopic> findAllByPlanIdOrderByWeekNumberAsc(UUID planId);
    Optional<PlanTopic> findByPlanIdAndTopicId(UUID planId, UUID topicId);
    Optional<PlanTopic> findByPlanIdAndWeekNumber(UUID planId, int weekNumber);
    Optional<PlanTopic> findByPlanIdAndActiveTrue(UUID planId);
    boolean existsByPlanIdAndTopicId(UUID planId, UUID topicId);
    boolean existsByPlanIdAndWeekNumber(UUID planId, int weekNumber);
    long countByTopicId(UUID topicId);
    void deleteByPlanIdAndTopicId(UUID planId, UUID topicId);
}
