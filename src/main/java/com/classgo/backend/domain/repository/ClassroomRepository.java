package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.Classroom;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassroomRepository extends JpaRepository<Classroom, UUID> {
    List<Classroom> findByTeacherUserId(UUID teacherUserId);
    Optional<Classroom> findByIdAndTeacherUserId(UUID id, UUID teacherUserId);
}
