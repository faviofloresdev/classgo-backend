package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.ParentStudentLink;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentStudentLinkRepository extends JpaRepository<ParentStudentLink, UUID> {
    boolean existsByParentIdAndStudentId(UUID parentId, UUID studentId);
    List<ParentStudentLink> findByParentUserId(UUID parentUserId);
    Optional<ParentStudentLink> findByParentUserIdAndStudentId(UUID parentUserId, UUID studentId);
}
