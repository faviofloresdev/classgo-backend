package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.PedagogicalTag;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedagogicalTagRepository extends JpaRepository<PedagogicalTag, UUID> {
    List<PedagogicalTag> findAllByTeacherIdOrderByNameAsc(UUID teacherId);
    List<PedagogicalTag> findAllByTeacherIdAndNameContainingIgnoreCaseOrderByNameAsc(UUID teacherId, String name);
    Optional<PedagogicalTag> findByIdAndTeacherId(UUID id, UUID teacherId);
    Optional<PedagogicalTag> findByTeacherIdAndSlug(UUID teacherId, String slug);
    boolean existsByTeacherIdAndSlug(UUID teacherId, String slug);
}
