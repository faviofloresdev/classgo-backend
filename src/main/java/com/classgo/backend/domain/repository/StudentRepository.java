package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.Student;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, UUID> {
    Optional<Student> findByStudentCode(String studentCode);
    List<Student> findByClassroomId(UUID classId);
}
