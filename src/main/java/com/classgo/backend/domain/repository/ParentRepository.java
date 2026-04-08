package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.Parent;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentRepository extends JpaRepository<Parent, UUID> {
    Optional<Parent> findByUserId(UUID userId);
}
