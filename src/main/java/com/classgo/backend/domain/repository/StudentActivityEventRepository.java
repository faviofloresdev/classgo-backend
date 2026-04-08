package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.StudentActivityEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentActivityEventRepository extends JpaRepository<StudentActivityEvent, UUID> {
    List<StudentActivityEvent> findBySessionIdOrderByHappenedAtAsc(UUID sessionId);
}
