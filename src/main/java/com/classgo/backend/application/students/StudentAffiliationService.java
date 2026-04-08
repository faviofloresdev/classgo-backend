package com.classgo.backend.application.students;

import com.classgo.backend.api.students.dto.StudentDtos.ChildResponse;
import com.classgo.backend.api.students.dto.StudentDtos.JoinStudentRequest;
import com.classgo.backend.api.students.dto.StudentDtos.UpdateChildRequest;
import com.classgo.backend.domain.enums.UserRole;
import com.classgo.backend.domain.model.Parent;
import com.classgo.backend.domain.model.ParentStudentLink;
import com.classgo.backend.domain.model.Student;
import com.classgo.backend.domain.repository.ParentRepository;
import com.classgo.backend.domain.repository.ParentStudentLinkRepository;
import com.classgo.backend.domain.repository.StudentRepository;
import com.classgo.backend.infrastructure.security.SecurityUtils;
import com.classgo.backend.shared.exception.BusinessRuleViolationException;
import com.classgo.backend.shared.exception.DuplicateResourceException;
import com.classgo.backend.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentAffiliationService {

    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;
    private final ParentStudentLinkRepository linkRepository;

    public StudentAffiliationService(
        ParentRepository parentRepository,
        StudentRepository studentRepository,
        ParentStudentLinkRepository linkRepository
    ) {
        this.parentRepository = parentRepository;
        this.studentRepository = studentRepository;
        this.linkRepository = linkRepository;
    }

    @Transactional
    public ChildResponse join(JoinStudentRequest request) {
        SecurityUtils.requireRole(UserRole.PARENT);
        Parent parent = parentRepository.findByUserId(SecurityUtils.currentUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Parent profile not found"));
        Student student = studentRepository.findByStudentCode(request.studentCode())
            .orElseThrow(() -> new ResourceNotFoundException("Student code not found"));
        if (!student.isActive()) {
            throw new BusinessRuleViolationException("Student is inactive");
        }
        if (linkRepository.existsByParentIdAndStudentId(parent.getId(), student.getId())) {
            throw new DuplicateResourceException("Student already linked to this parent");
        }
        ParentStudentLink link = new ParentStudentLink();
        link.setParent(parent);
        link.setStudent(student);
        link.setDisplayName(request.displayName());
        link.setAvatarId(request.avatarId());
        return toResponse(linkRepository.save(link));
    }

    public List<ChildResponse> myChildren() {
        SecurityUtils.requireRole(UserRole.PARENT);
        return linkRepository.findByParentUserId(SecurityUtils.currentUserId()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public ChildResponse update(UUID studentId, UpdateChildRequest request) {
        SecurityUtils.requireRole(UserRole.PARENT);
        ParentStudentLink link = linkRepository.findByParentUserIdAndStudentId(SecurityUtils.currentUserId(), studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student link not found"));
        link.setDisplayName(request.displayName());
        link.setAvatarId(request.avatarId());
        link.setNickname(request.nickname());
        return toResponse(linkRepository.save(link));
    }

    public ParentStudentLink getParentLinkOrThrow(UUID userId, UUID studentId) {
        return linkRepository.findByParentUserIdAndStudentId(userId, studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student is not linked to this parent"));
    }

    private ChildResponse toResponse(ParentStudentLink link) {
        return new ChildResponse(
            link.getStudent().getId(),
            link.getStudent().getClassroom().getId(),
            link.getStudent().getClassroom().getName(),
            link.getDisplayName(),
            link.getAvatarId(),
            link.getNickname()
        );
    }
}
