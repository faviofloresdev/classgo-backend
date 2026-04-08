package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.Subject;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {
}
