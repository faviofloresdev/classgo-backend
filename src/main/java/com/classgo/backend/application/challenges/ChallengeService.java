package com.classgo.backend.application.challenges;

import com.classgo.backend.api.challenges.dto.ChallengeDtos.ChallengeResponse;
import com.classgo.backend.api.challenges.dto.ChallengeDtos.CreateChallengeRequest;
import com.classgo.backend.application.notifications.NotificationService;
import com.classgo.backend.domain.enums.ChallengeStatus;
import com.classgo.backend.domain.enums.NotificationType;
import com.classgo.backend.domain.enums.UserRole;
import com.classgo.backend.domain.model.ChallengeParticipant;
import com.classgo.backend.domain.model.Classroom;
import com.classgo.backend.domain.model.ParentStudentLink;
import com.classgo.backend.domain.model.Student;
import com.classgo.backend.domain.model.Teacher;
import com.classgo.backend.domain.model.Topic;
import com.classgo.backend.domain.model.WeeklyChallenge;
import com.classgo.backend.domain.repository.ChallengeParticipantRepository;
import com.classgo.backend.domain.repository.ClassroomRepository;
import com.classgo.backend.domain.repository.ParentStudentLinkRepository;
import com.classgo.backend.domain.repository.StudentRepository;
import com.classgo.backend.domain.repository.TeacherRepository;
import com.classgo.backend.domain.repository.TopicRepository;
import com.classgo.backend.domain.repository.WeeklyChallengeRepository;
import com.classgo.backend.infrastructure.security.SecurityUtils;
import com.classgo.backend.shared.exception.BusinessRuleViolationException;
import com.classgo.backend.shared.exception.ResourceNotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChallengeService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ChallengeService.class);
    private final WeeklyChallengeRepository weeklyChallengeRepository;
    private final ClassroomRepository classroomRepository;
    private final TopicRepository topicRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final ChallengeParticipantRepository participantRepository;
    private final ParentStudentLinkRepository linkRepository;
    private final NotificationService notificationService;

    public ChallengeService(WeeklyChallengeRepository weeklyChallengeRepository, ClassroomRepository classroomRepository, TopicRepository topicRepository, TeacherRepository teacherRepository, StudentRepository studentRepository, ChallengeParticipantRepository participantRepository, ParentStudentLinkRepository linkRepository, NotificationService notificationService) {
        this.weeklyChallengeRepository = weeklyChallengeRepository;
        this.classroomRepository = classroomRepository;
        this.topicRepository = topicRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.participantRepository = participantRepository;
        this.linkRepository = linkRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public ChallengeResponse create(CreateChallengeRequest request) {
        SecurityUtils.requireRole(UserRole.TEACHER);
        Classroom classroom = classroomRepository.findByIdAndTeacherUserId(request.classId(), SecurityUtils.currentUserId()).orElseThrow(() -> new ResourceNotFoundException("Classroom not found"));
        Topic topic = topicRepository.findById(request.topicId()).orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
        Teacher teacher = teacherRepository.findByUserId(SecurityUtils.currentUserId()).orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found"));
        validateChallengeDates(request.startDate(), request.endDate());
        WeeklyChallenge challenge = new WeeklyChallenge();
        challenge.setClassroom(classroom);
        challenge.setTopic(topic);
        challenge.setTitle(request.title());
        challenge.setDescription(request.description());
        challenge.setStartDate(request.startDate());
        challenge.setEndDate(request.endDate());
        challenge.setCreatedByTeacher(teacher);
        return toResponse(weeklyChallengeRepository.save(challenge));
    }

    @Transactional
    public ChallengeResponse publish(UUID challengeId) {
        SecurityUtils.requireRole(UserRole.TEACHER);
        WeeklyChallenge challenge = weeklyChallengeRepository.findByIdAndCreatedByTeacherUserId(challengeId, SecurityUtils.currentUserId()).orElseThrow(() -> new ResourceNotFoundException("Challenge not found"));
        if (challenge.getStatus() != ChallengeStatus.DRAFT) {
            throw new BusinessRuleViolationException("Only draft challenges can be published");
        }
        weeklyChallengeRepository.findFirstByStatusAndClassroomIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(ChallengeStatus.PUBLISHED, challenge.getClassroom().getId(), challenge.getEndDate(), challenge.getStartDate()).ifPresent(existing -> {
            throw new BusinessRuleViolationException("Another published challenge overlaps with this challenge");
        });
        List<Student> students = studentRepository.findByClassroomId(challenge.getClassroom().getId()).stream().filter(Student::isActive).toList();
        for (Student student : students) {
            ChallengeParticipant participant = new ChallengeParticipant();
            participant.setChallenge(challenge);
            participant.setStudent(student);
            participantRepository.save(participant);
        }
        challenge.setStatus(ChallengeStatus.PUBLISHED);
        challenge.setPublishedAt(Instant.now());
        WeeklyChallenge saved = weeklyChallengeRepository.save(challenge);
        List<ParentStudentLink> familyLinks = linkRepository.findAll().stream().filter(link -> students.stream().anyMatch(student -> student.getId().equals(link.getStudent().getId()))).toList();
        familyLinks.forEach(link -> notificationService.send(link.getParent().getUser(), link.getParent().getUser().getEmail(), NotificationType.CHALLENGE_PUBLISHED, "New challenge published", "<p>A new weekly challenge is ready for " + link.getDisplayName() + "</p>", "{\"challengeId\":\"" + saved.getId() + "\"}"));
        log.info("Challenge published {}", saved.getId());
        return toResponse(saved);
    }

    public ChallengeResponse get(UUID challengeId) {
        return toResponse(weeklyChallengeRepository.findById(challengeId).orElseThrow(() -> new ResourceNotFoundException("Challenge not found")));
    }

    public ChallengeResponse active(UUID studentId) {
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        WeeklyChallenge challenge = weeklyChallengeRepository.findFirstByStatusAndClassroomId(ChallengeStatus.PUBLISHED, student.getClassroom().getId()).orElseThrow(() -> new ResourceNotFoundException("No active challenge found"));
        return toResponse(challenge);
    }

    private void validateChallengeDates(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessRuleViolationException("End date must be after start date");
        }
    }

    private ChallengeResponse toResponse(WeeklyChallenge challenge) {
        return new ChallengeResponse(challenge.getId(), challenge.getClassroom().getId(), challenge.getTopic().getId(), challenge.getTitle(), challenge.getDescription(), challenge.getStatus(), challenge.getStartDate(), challenge.getEndDate(), challenge.getPublishedAt(), challenge.getClosedAt());
    }
}
